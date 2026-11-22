#!/usr/bin/env bash
# Verificação pós-deploy (smoke test), executada pelo Jenkins na rede vinheria-net:
# espera cada serviço reciclar para a versão publicada e confere saúde e regras de acesso.
# Uso: verificar-deploy.sh <versao>
set -uo pipefail

VERSAO="${1:?informe a versão publicada}"
CA="${CA_CERT:-/run/secrets/tls/ca.crt}"
TEMPO_LIMITE="${TEMPO_LIMITE:-120}"

# HTTPS (modo padrão) com o certificado verificado pela CA interna; HTTP só no modo inseguro de demonstração.
url_base() {
    local servico="$1"
    # --fail: só considera o serviço disponível se o /health responder 2xx.
    if curl -sf -o /dev/null --max-time 3 --cacert "$CA" "https://$servico.vinheria.local:8443/health"; then
        echo "https://$servico.vinheria.local:8443"
    else
        echo "http://$servico.vinheria.local:8080"
    fi
}

versao_em_execucao() {
    curl -sf --max-time 3 --cacert "$CA" "$1/versao" | sed -nE 's/.*"versao":"([^"]*)".*/\1/p'
}

for SERVICO in identidade catalogo; do
    ATUAL=""
    # Prazo pelo relógio (SECONDS), não por número de voltas: cada volta pode levar até ~9 s com os timeouts.
    PRAZO=$((SECONDS + TEMPO_LIMITE))
    while [ "$SECONDS" -lt "$PRAZO" ]; do
        BASE=$(url_base "$SERVICO")
        ATUAL=$(versao_em_execucao "$BASE")
        [ "$ATUAL" = "$VERSAO" ] && break
        sleep 3
    done
    if [ "$ATUAL" != "$VERSAO" ]; then
        echo "✗ $SERVICO não assumiu a versão $VERSAO em ${TEMPO_LIMITE}s (em execução: ${ATUAL:-sem resposta})"
        exit 1
    fi
    echo "✔ $SERVICO em execução na versão $VERSAO ($BASE)"
done

# Regras de acesso continuam valendo depois do deploy
CATALOGO=$(url_base catalogo)
LEITURA=$(curl -s -o /dev/null -w '%{http_code}' --cacert "$CA" "$CATALOGO/api/v1/catalogo/produtos")
ESCRITA=$(curl -s -o /dev/null -w '%{http_code}' --cacert "$CA" -H 'Content-Type: application/json' \
          -d '{"nome":"smoke","tipo":"Tinto","preco":1}' "$CATALOGO/api/v1/catalogo/produtos")
echo "GET produtos sem token -> $LEITURA (esperado 200) | POST sem token -> $ESCRITA (esperado 401)"
[ "$LEITURA" = "200" ] && [ "$ESCRITA" = "401" ] || { echo "✗ regras de acesso inesperadas"; exit 1; }
echo "✔ Deploy da versão $VERSAO verificado."
