#!/usr/bin/env bash
# Etapa de segurança do pipeline (DevSecOps): falha o build se algum pacote NuGet usado pelos
# microsserviços (inclusive dependências transitivas) tiver vulnerabilidade conhecida.
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJETOS=(
    "identidade-api/IdentidadeApi/IdentidadeApi.csproj"
    "csharp-api/VinheriaApi/VinheriaApi.csproj"
)
ENCONTRADAS=0

for PROJETO in "${PROJETOS[@]}"; do
    echo "==> $PROJETO"
    # Qualquer erro da consulta (ex.: sem acesso ao feed) interrompe o script: falha fechada.
    SAIDA=$(dotnet list "$RAIZ/$PROJETO" package --vulnerable --include-transitive 2>&1)
    echo "$SAIDA"
    if echo "$SAIDA" | grep -qi "has the following vulnerable packages"; then
        ENCONTRADAS=1
    fi
done

if [ "$ENCONTRADAS" -ne 0 ]; then
    echo "✗ Pacotes com vulnerabilidades conhecidas: atualize-os antes de publicar."
    exit 1
fi
echo "✔ Nenhum pacote vulnerável encontrado."
