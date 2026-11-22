#!/usr/bin/env bash
# Gera o tráfego da demonstração no Wireshark (captura na interface br-vinheria):
#   1. login com e-mail e senha            -> Identidade devolve JWT (corpo) + refresh token (Set-Cookie)
#   2. cadastro de produto com o JWT       -> Catálogo busca o JWKS na Identidade e valida o token
#   3. renovação da sessão pelo cookie     -> novo JWT + refresh token rotacionado
#   4. logout                              -> refresh token revogado
# Funciona nos dois modos: HTTPS (padrão) e HTTP (docker-compose.inseguro.yml).
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CA="$RAIZ/infra/secrets/tls/ca.crt"
J='Content-Type: application/json'

if curl -s -o /dev/null --max-time 3 --cacert "$CA" https://localhost:8441/health; then
    MODO="HTTPS"; ID=https://localhost:8441; CAT=https://localhost:8442; CURL=(curl -sS --fail --cacert "$CA")
else
    MODO="HTTP (inseguro)"; ID=http://127.0.0.1:8081; CAT=http://127.0.0.1:8082; CURL=(curl -sS --fail)
fi
# --fail: qualquer resposta 4xx/5xx interrompe o script (set -e), em vez de terminar com "Tráfego gerado".
echo "Modo: $MODO"

cookie_de() { sed -nE 's/^[Ss]et-[Cc]ookie: vinheria_refresh=([^;]*).*/\1/p' "$1" | tr -d '\r'; }
CABECALHOS=$(mktemp); trap 'rm -f "$CABECALHOS"' EXIT

echo "1) POST $ID/api/v1/identidade/login  (admin@agnello.com / admin123)"
RESPOSTA=$("${CURL[@]}" -D "$CABECALHOS" -H "$J" -d '{"email":"admin@agnello.com","senha":"admin123"}' "$ID/api/v1/identidade/login")
TOKEN=$(echo "$RESPOSTA" | sed -nE 's/.*"accessToken":"([^"]+)".*/\1/p')
REFRESH=$(cookie_de "$CABECALHOS")
echo "   JWT recebido: ${TOKEN:0:40}..."

echo "2) POST $CAT/api/v1/catalogo/produtos  (Authorization: Bearer <JWT>)"
PRODUTO=$("${CURL[@]}" -H "$J" -H "Authorization: Bearer $TOKEN" \
    -d '{"nome":"Vinho da Demonstração","tipo":"Tinto","preco":99.9,"estoque":3}' "$CAT/api/v1/catalogo/produtos")
ID_PRODUTO=$(echo "$PRODUTO" | sed -nE 's/.*"id":([0-9]+).*/\1/p')
echo "   produto criado: id ${ID_PRODUTO:-?}"

echo "3) POST $ID/api/v1/identidade/refresh  (Cookie: vinheria_refresh=...)"
"${CURL[@]}" -o /dev/null -D "$CABECALHOS" -X POST -H "Cookie: vinheria_refresh=$REFRESH" "$ID/api/v1/identidade/refresh"
NOVO_REFRESH=$(cookie_de "$CABECALHOS")
echo "   refresh token rotacionado: $([ "$NOVO_REFRESH" != "$REFRESH" ] && echo sim || echo não)"

echo "4) POST $ID/api/v1/identidade/logout"
"${CURL[@]}" -o /dev/null -X POST -H "Cookie: vinheria_refresh=$NOVO_REFRESH" "$ID/api/v1/identidade/logout"

# Limpeza do produto de demonstração
[ -n "${ID_PRODUTO:-}" ] && "${CURL[@]}" -o /dev/null -X DELETE -H "Authorization: Bearer $TOKEN" "$CAT/api/v1/catalogo/produtos/$ID_PRODUTO"
echo "Tráfego gerado. Pare a captura no Wireshark."
