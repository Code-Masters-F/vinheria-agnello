#!/usr/bin/env bash
# Etapa de segurança do pipeline (DevSecOps): falha o build se algum pacote NuGet usado pelos
# microsserviços (inclusive dependências transitivas) tiver vulnerabilidade conhecida.
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# Projetos de teste incluídos: o pipeline também compila e executa as dependências deles.
PROJETOS=(
    "identidade-api/IdentidadeApi/IdentidadeApi.csproj"
    "identidade-api/IdentidadeApi.Tests/IdentidadeApi.Tests.csproj"
    "csharp-api/VinheriaApi/VinheriaApi.csproj"
    "csharp-api/VinheriaApi.Tests/VinheriaApi.Tests.csproj"
)
ENCONTRADAS=0
SEM_DADOS=0

for PROJETO in "${PROJETOS[@]}"; do
    echo "==> $PROJETO"
    # Qualquer erro da consulta (ex.: sem acesso ao feed) interrompe o script: falha fechada.
    SAIDA=$(dotnet list "$RAIZ/$PROJETO" package --vulnerable --include-transitive 2>&1)
    echo "$SAIDA"
    if echo "$SAIDA" | grep -qi "has the following vulnerable packages"; then
        ENCONTRADAS=1
    fi
    # Sem os dados de vulnerabilidade (feed fora do ar, aviso NU1900/NU1905) o resultado não vale como "limpo".
    if echo "$SAIDA" | grep -qiE "NU19[0-9]{2}|warn"; then
        SEM_DADOS=1
    fi
done

if [ "$SEM_DADOS" -ne 0 ]; then
    echo "✗ Não foi possível obter os dados de vulnerabilidade de todos os projetos: publicação bloqueada."
    exit 1
fi

if [ "$ENCONTRADAS" -ne 0 ]; then
    echo "✗ Pacotes com vulnerabilidades conhecidas: atualize-os antes de publicar."
    exit 1
fi
echo "✔ Nenhum pacote vulnerável encontrado."
