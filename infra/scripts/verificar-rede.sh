#!/usr/bin/env bash
# Mostra as evidências da infraestrutura simulada (útil para as capturas de tela do relatório):
# concessões DHCP, resolução DNS, TLS, comunicação entre os serviços e o fluxo de autenticação.
# Detecta sozinho se o ambiente está no modo seguro (HTTPS) ou no modo inseguro de demonstração (HTTP).
set -uo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CA="$RAIZ/infra/secrets/tls/ca.crt"

titulo() { printf '\n\033[1;35m== %s ==\033[0m\n' "$1"; }

if curl -s -o /dev/null --max-time 3 --cacert "$CA" https://localhost:8441/health; then
    MODO="HTTPS (seguro)"
    URL_ID=https://localhost:8441; URL_CAT=https://localhost:8442
    URL_INTERNA=https://identidade.vinheria.local:8443
    CURL=(curl -s --max-time 5 --cacert "$CA")
else
    MODO="HTTP (inseguro, demonstração)"
    URL_ID=http://127.0.0.1:8081; URL_CAT=http://127.0.0.1:8082
    URL_INTERNA=http://identidade.vinheria.local:8080
    CURL=(curl -s --max-time 5)
fi

titulo "Modo do ambiente: $MODO"
docker ps --filter name=vinheria- --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

titulo "Rede vinheria-net (Docker)"
docker network inspect vinheria-net --format \
  'sub-rede {{(index .IPAM.Config 0).Subnet}} | gateway {{(index .IPAM.Config 0).Gateway}} | interface no host: {{index .Options "com.docker.network.bridge.name"}}'

titulo "Concessões DHCP entregues pelo dnsmasq (expiração | MAC | IP | hostname)"
docker exec vinheria-dns cat /var/lib/misc/dnsmasq.leases

titulo "Troca DHCP registrada no log do dnsmasq"
docker logs vinheria-dns 2>&1 | grep -E "DHCP(DISCOVER|OFFER|REQUEST|ACK)" | tail -8

titulo "Endereços IPv4 dos serviços (IP do Docker + IP recebido por DHCP)"
for s in identidade catalogo; do
    printf '%-11s ' "$s:"
    docker exec "vinheria-$s" ip -4 -o addr show eth0 | awk '{printf "%s  ", $4}'
    echo
done

titulo "Resolução DNS (consultada a partir do Catálogo)"
for nome in identidade catalogo db dns; do
    ip=$(docker exec vinheria-catalogo nslookup "$nome.vinheria.local" 2>/dev/null | awk '/^Address/ && !/#53/ {print $2}' | tail -1)
    printf '%-26s -> %s\n' "$nome.vinheria.local" "${ip:-NÃO RESOLVIDO}"
done

if [[ "$MODO" == HTTPS* ]]; then
    titulo "Certificado apresentado pela Identidade (emitido pela CA interna)"
    openssl s_client -connect localhost:8441 -servername identidade.vinheria.local -CAfile "$CA" </dev/null 2>/dev/null \
        | grep -E "^(subject|issuer)=|Verify return code|^New, |Protocol" | sed 's/^ *//'

    titulo "HTTP desativado no modo seguro"
    printf 'GET http://127.0.0.1:8081/health -> %s\n' \
        "$(curl -s -o /dev/null --max-time 3 -w '%{http_code}' http://127.0.0.1:8081/health || true)  (000 = conexão recusada)"
fi

titulo "Comunicação Catálogo -> Identidade pelo nome (descoberta OIDC usada para validar tokens)"
# No modo HTTPS, o wget do container verifica o certificado da Identidade contra a CA interna
docker exec -e SSL_CERT_FILE=/run/secrets/tls/ca.crt vinheria-catalogo wget -qO- "$URL_INTERNA/.well-known/openid-configuration"
echo

titulo "Fluxo de autenticação (host -> serviços pelas portas publicadas)"
J='Content-Type: application/json'
TOKEN=$("${CURL[@]}" -H "$J" -d '{"email":"admin@agnello.com","senha":"admin123"}' "$URL_ID/api/v1/identidade/login" \
        | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')
echo "login admin@agnello.com          -> token ${TOKEN:0:30}..."
printf 'GET produtos (sem token)         -> HTTP %s\n' "$("${CURL[@]}" -o /dev/null -w '%{http_code}' "$URL_CAT/api/v1/catalogo/produtos")"
printf 'POST produto (sem token)         -> HTTP %s\n' "$("${CURL[@]}" -o /dev/null -w '%{http_code}' -H "$J" -d '{"nome":"Teste","tipo":"Tinto","preco":10}' "$URL_CAT/api/v1/catalogo/produtos")"
RESPOSTA=$("${CURL[@]}" -w ' %{http_code}' -H "$J" -H "Authorization: Bearer $TOKEN" -d '{"nome":"Vinho da verificação","tipo":"Tinto","preco":10}' "$URL_CAT/api/v1/catalogo/produtos")
printf 'POST produto (token de Admin)    -> HTTP %s\n' "${RESPOSTA##* }"
ID=$(echo "$RESPOSTA" | sed -nE 's/.*"id":([0-9]+).*/\1/p')
[ -n "$ID" ] && printf 'DELETE produto %-4s (Admin)       -> HTTP %s\n' "$ID" \
    "$("${CURL[@]}" -o /dev/null -w '%{http_code}' -X DELETE -H "Authorization: Bearer $TOKEN" "$URL_CAT/api/v1/catalogo/produtos/$ID")"
