#!/usr/bin/env bash
# Prepara os segredos locais usados pelo docker-compose. Nada disso é versionado:
#   infra/secrets/jwt-privada.pem  chave RSA que a Identidade usa para assinar os tokens
#   .env                           senhas do PostgreSQL (superusuário + um usuário por serviço)
# Pode ser executado mais de uma vez: só cria o que estiver faltando.
set -euo pipefail

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

touch "$ENV"
# Garante quebra de linha no fim antes de acrescentar variáveis
[ -s "$ENV" ] && [ -n "$(tail -c1 "$ENV")" ] && echo >> "$ENV"

for VARIAVEL in POSTGRES_PASSWORD IDENTIDADE_DB_PASSWORD CATALOGO_DB_PASSWORD; do
    if grep -q "^${VARIAVEL}=" "$ENV"; then
        echo "• $VARIAVEL já definida no .env"
    else
        echo "${VARIAVEL}=$(openssl rand -hex 16)" >> "$ENV"
        echo "✔ $VARIAVEL gerada no .env"
    fi
done

echo
echo "Pronto. Suba o ambiente com: docker compose up -d --build"
