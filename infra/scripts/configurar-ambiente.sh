#!/usr/bin/env bash
# Prepara os segredos locais usados pelo docker-compose. Nada disso é versionado:
#   infra/secrets/jwt-privada.pem      chave RSA que a Identidade usa para assinar os tokens
#   infra/secrets/tls/ca.crt + ca.key  CA interna da Vinheria (a chave da CA nunca vai para os containers)
#   infra/secrets/tls/<servico>.crt/.key  certificado HTTPS de cada serviço, emitido pela CA interna
#   .env                               senhas do PostgreSQL e do admin do Jenkins
#   publicacao/<servico>/              pasta de produção + deploy inicial (versão 0) dos serviços
# Pode ser executado mais de uma vez: só cria o que estiver faltando.
set -euo pipefail

# Git Bash (Windows) converte argumentos que começam com "/" em caminhos; o -subj do openssl precisa ficar intacto.
export MSYS_NO_PATHCONV=1

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SEGREDOS="$RAIZ/infra/secrets"
ENV="$RAIZ/.env"

mkdir -p "$SEGREDOS"
chmod 700 "$SEGREDOS"

if [ ! -f "$SEGREDOS/jwt-privada.pem" ]; then
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$SEGREDOS/jwt-privada.pem" 2>/dev/null
    echo "✔ chave JWT criada em infra/secrets/jwt-privada.pem"
else
    echo "• chave JWT já existe"
fi
# O serviço roda como usuário não-root (uid 1654) dentro do container e precisa ler a chave.
# A pasta com permissão 700 impede que outros usuários do host cheguem até o arquivo.
chmod 644 "$SEGREDOS/jwt-privada.pem"

# ---------- Certificados HTTPS (CA interna + um certificado por serviço) ----------
TLS="$SEGREDOS/tls"
mkdir -p "$TLS"

if [ ! -f "$TLS/ca.crt" ]; then
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$TLS/ca.key" 2>/dev/null
    openssl req -x509 -new -key "$TLS/ca.key" -sha256 -days 825 -out "$TLS/ca.crt" \
        -subj "/O=Vinheria Agnello/CN=Vinheria Agnello CA Interna" \
        -addext "basicConstraints=critical,CA:TRUE" \
        -addext "keyUsage=critical,keyCertSign,cRLSign"
    echo "✔ CA interna criada em infra/secrets/tls/ca.crt"
else
    echo "• CA interna já existe"
fi

for SERVICO in identidade catalogo; do
    if [ ! -f "$TLS/$SERVICO.crt" ]; then
        openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$TLS/$SERVICO.key" 2>/dev/null
        openssl req -new -key "$TLS/$SERVICO.key" -out "$TLS/$SERVICO.csr" \
            -subj "/O=Vinheria Agnello/CN=$SERVICO.vinheria.local"
        # SAN: nome na rede interna + localhost/127.0.0.1 para acesso pelo host (portas publicadas)
        printf '%s\n' \
            "basicConstraints=critical,CA:FALSE" \
            "keyUsage=critical,digitalSignature,keyEncipherment" \
            "extendedKeyUsage=serverAuth" \
            "subjectAltName=DNS:$SERVICO.vinheria.local,DNS:localhost,IP:127.0.0.1" > "$TLS/$SERVICO.ext"
        openssl x509 -req -in "$TLS/$SERVICO.csr" -CA "$TLS/ca.crt" -CAkey "$TLS/ca.key" -CAcreateserial \
            -out "$TLS/$SERVICO.crt" -days 397 -sha256 -extfile "$TLS/$SERVICO.ext" 2>/dev/null
        rm -f "$TLS/$SERVICO.csr" "$TLS/$SERVICO.ext"
        echo "✔ certificado HTTPS de $SERVICO.vinheria.local emitido pela CA interna"
    else
        echo "• certificado de $SERVICO já existe"
    fi
done

# Chave da CA: somente o dono. Certificados e chaves dos serviços: legíveis pelo usuário não-root
# dos containers (a pasta infra/secrets com permissão 700 protege contra outros usuários do host).
chmod 600 "$TLS/ca.key"
chmod 644 "$TLS"/*.crt "$TLS/identidade.key" "$TLS/catalogo.key"

# ---------- Senhas do PostgreSQL ----------
touch "$ENV"
# Garante quebra de linha no fim antes de acrescentar variáveis
[ -s "$ENV" ] && [ -n "$(tail -c1 "$ENV")" ] && echo >> "$ENV"

for VARIAVEL in POSTGRES_PASSWORD IDENTIDADE_DB_PASSWORD CATALOGO_DB_PASSWORD JENKINS_ADMIN_PASSWORD; do
    if grep -q "^${VARIAVEL}=" "$ENV"; then
        echo "• $VARIAVEL já definida no .env"
    else
        echo "${VARIAVEL}=$(openssl rand -hex 16)" >> "$ENV"
        echo "✔ $VARIAVEL gerada no .env"
    fi
done

# ---------- Pasta de publicação + deploy inicial ----------
for SERVICO in identidade catalogo; do
    mkdir -p "$RAIZ/publicacao/$SERVICO/releases"
done
# O Jenkins grava na pasta com o uid 1000; em hosts Linux com outro uid, libera a escrita.
if [ "$(uname -s)" = "Linux" ] && [ "$(id -u)" != "1000" ]; then
    chmod -R a+rwX "$RAIZ/publicacao"
    echo "• publicacao/ liberada para escrita do Jenkins (uid do host diferente de 1000)"
fi
for SERVICO in identidade catalogo; do
    if [ ! -e "$RAIZ/publicacao/$SERVICO/atual" ]; then
        echo "→ deploy inicial de $SERVICO (versão 0)"
        bash "$RAIZ/infra/scripts/publicar.sh" "$SERVICO" 0 > /dev/null
        echo "✔ $SERVICO publicado em publicacao/$SERVICO/releases/0"
    else
        echo "• $SERVICO já publicado ($(readlink "$RAIZ/publicacao/$SERVICO/atual"))"
    fi
done

echo
echo "Pronto. Suba o ambiente com: docker compose up -d --build"
echo "Jenkins: http://localhost:8090  (usuário admin, senha JENKINS_ADMIN_PASSWORD do .env)"
