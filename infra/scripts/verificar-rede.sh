#!/usr/bin/env bash
# Mostra as evidências da infraestrutura simulada (útil para as capturas de tela do relatório):
# concessões DHCP, resolução DNS, comunicação entre os serviços e o fluxo de autenticação.
set -uo pipefail

titulo() { printf '\n\033[1;35m== %s ==\033[0m\n' "$1"; }

titulo "Containers"
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

titulo "Comunicação Catálogo -> Identidade pelo nome (descoberta OIDC usada para validar tokens)"
docker exec vinheria-catalogo wget -qO- http://identidade.vinheria.local:8080/.well-known/openid-configuration
echo

titulo "Fluxo de autenticação (host -> serviços pelas portas publicadas)"
J='Content-Type: application/json'
TOKEN=$(curl -s -H "$J" -d '{"email":"admin@agnello.com","senha":"admin123"}' http://127.0.0.1:8081/api/v1/identidade/login \
        | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')
echo "login admin@agnello.com          -> token ${TOKEN:0:30}..."
printf 'GET produtos (sem token)         -> HTTP %s\n' "$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8082/api/v1/catalogo/produtos)"
printf 'POST produto (sem token)         -> HTTP %s\n' "$(curl -s -o /dev/null -w '%{http_code}' -H "$J" -d '{"nome":"Teste","tipo":"Tinto","preco":10}' http://127.0.0.1:8082/api/v1/catalogo/produtos)"
RESPOSTA=$(curl -s -w ' %{http_code}' -H "$J" -H "Authorization: Bearer $TOKEN" -d '{"nome":"Vinho da verificação","tipo":"Tinto","preco":10}' http://127.0.0.1:8082/api/v1/catalogo/produtos)
printf 'POST produto (token de Admin)    -> HTTP %s\n' "${RESPOSTA##* }"
ID=$(echo "$RESPOSTA" | sed -E 's/.*"id":([0-9]+).*/\1/')
[ -n "$ID" ] && printf 'DELETE produto %-4s (Admin)       -> HTTP %s\n' "$ID" \
    "$(curl -s -o /dev/null -w '%{http_code}' -X DELETE -H "Authorization: Bearer $TOKEN" http://127.0.0.1:8082/api/v1/catalogo/produtos/$ID)"
