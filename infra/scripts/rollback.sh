#!/usr/bin/env bash
# Rollback de um microsserviço: aponta o link "atual" para uma versão já publicada.
# O servidor de aplicação (infra/runtime/executar.sh) detecta a troca e recicla o processo.
#
# Uso: rollback.sh <identidade|catalogo> [versao]
#      sem versão: volta para a versão publicada imediatamente anterior à atual
# Observação: o próximo build do pipeline publica a versão mais nova novamente.
set -euo pipefail

SERVICO="${1:?informe o serviço: identidade ou catalogo}"
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PASTA="${PUBLICACAO:-$RAIZ/publicacao}/$SERVICO"

[ -L "$PASTA/atual" ] || { echo "Nenhuma versão publicada para $SERVICO em $PASTA" >&2; exit 1; }
ATUAL=$(basename "$(readlink "$PASTA/atual")")

if [ -n "${2:-}" ]; then
    ALVO="$2"
else
    ALVO=$(ls -1 "$PASTA/releases" | grep -E '^[0-9]+$' | sort -n | awk -v atual="$ATUAL" '$1 < atual' | tail -1)
fi

if [ -z "$ALVO" ] || [ ! -d "$PASTA/releases/$ALVO" ]; then
    echo "Versão '${ALVO:-anterior}' não disponível. Versões publicadas:" >&2
    ls -1 "$PASTA/releases" >&2
    exit 1
fi

ln -sfn "releases/$ALVO" "$PASTA/atual.novo"
# GNU (Linux/WSL): mv -T troca o link de forma atômica. macOS/BSD: ln -sfn (troca quase instantânea).
mv -Tf "$PASTA/atual.novo" "$PASTA/atual" 2>/dev/null \
    || { rm -f "$PASTA/atual.novo"; ln -sfn "releases/$ALVO" "$PASTA/atual"; }
echo "$SERVICO: versão $ATUAL -> $ALVO (o serviço recicla em poucos segundos)"
