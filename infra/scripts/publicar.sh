#!/usr/bin/env bash
# =====================================================================
# Deploy de um microsserviço na pasta de publicação (simula o deploy em um servidor IIS)
# =====================================================================
#
# Uso: publicar.sh <identidade|catalogo> <versao>
#
# Estrutura gerada (PUBLICACAO = ./publicacao no host; /publicacao no Jenkins):
#   <servico>/releases/<versao>/   arquivos publicados (dotnet publish, Release)
#   <servico>/atual -> releases/<versao>   link trocado de forma atômica ao final
# O servidor de aplicação (infra/runtime/executar.sh) detecta a troca do link e recicla o processo.
# As 5 versões mais recentes são mantidas para rollback (basta apontar "atual" para uma delas).
set -euo pipefail

SERVICO="${1:?informe o serviço: identidade ou catalogo}"
VERSAO="${2:?informe a versão (ex.: número do build)}"
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PUBLICACAO="${PUBLICACAO:-$RAIZ/publicacao}"
MANTER=5

# A versão vira nome de pasta (e vai para um "sh -c" no modo sem .NET): só números, sem "../" nem aspas.
[[ "$VERSAO" =~ ^[0-9]+$ ]] || { echo "Versão inválida: '$VERSAO' (use apenas números, ex.: número do build)" >&2; exit 2; }

case "$SERVICO" in
    identidade) PROJETO="identidade-api/IdentidadeApi/IdentidadeApi.csproj" ;;
    catalogo)   PROJETO="csharp-api/VinheriaApi/VinheriaApi.csproj" ;;
    *) echo "Serviço desconhecido: $SERVICO" >&2; exit 2 ;;
esac

# Sem .NET SDK na máquina: executa este mesmo script dentro do container oficial do SDK 8.
if ! command -v dotnet >/dev/null 2>&1; then
    echo "dotnet não encontrado; publicando via container mcr.microsoft.com/dotnet/sdk:8.0"
    mkdir -p "$PUBLICACAO"
    exec docker run --rm --security-opt label=disable \
        -e PUBLICACAO=/publicacao -e DONO="$(id -u):$(id -g)" \
        -v "$RAIZ:/repo:ro" -v "$PUBLICACAO:/publicacao" mcr.microsoft.com/dotnet/sdk:8.0 \
        sh -c "cp -r /repo /work && cd /work && find . -type d \( -name bin -o -name obj \) -prune -exec rm -rf {} + \
               && bash infra/scripts/publicar.sh '$SERVICO' '$VERSAO' && chown -R \"\$DONO\" /publicacao"
fi

DESTINO_SERVICO="$PUBLICACAO/$SERVICO"
RELEASE="$DESTINO_SERVICO/releases/$VERSAO"
mkdir -p "$DESTINO_SERVICO/releases"

echo "==> Publicando $SERVICO versão $VERSAO em $RELEASE"
rm -rf "$RELEASE.tmp"
# [TokenOptimization] - Publica direto na pasta final temporária, sem cópias intermediárias.
dotnet publish "$RAIZ/$PROJETO" -c Release -o "$RELEASE.tmp" --nologo
rm -f "$RELEASE.tmp/appsettings.Development.json"
echo "$VERSAO" > "$RELEASE.tmp/VERSAO"
rm -rf "$RELEASE"
mv "$RELEASE.tmp" "$RELEASE"

# Troca atômica do link "atual": o servidor nunca vê uma versão pela metade.
# Link temporário com o PID no nome: um rollback rodando ao mesmo tempo não usa o mesmo arquivo.
TEMPORARIO="$DESTINO_SERVICO/atual.novo.$$"
ln -sfn "releases/$VERSAO" "$TEMPORARIO"
# GNU (Linux/WSL): mv -T troca o link de forma atômica. macOS/BSD: ln -sfn (troca quase instantânea).
mv -Tf "$TEMPORARIO" "$DESTINO_SERVICO/atual" 2>/dev/null \
    || { rm -f "$TEMPORARIO"; ln -sfn "releases/$VERSAO" "$DESTINO_SERVICO/atual"; }
echo "==> $SERVICO/atual -> releases/$VERSAO"

# Mantém apenas as últimas $MANTER versões (rollback)
# (awk em vez de "head -n -N", que não existe no macOS)
ls -1 "$DESTINO_SERVICO/releases" | grep -E '^[0-9]+$' | sort -n \
    | awk -v manter="$MANTER" '{ v[NR] = $0 } END { for (i = 1; i <= NR - manter; i++) print v[i] }' \
    | while read -r ANTIGA; do rm -rf "${DESTINO_SERVICO:?}/releases/$ANTIGA"; echo "    removida versão antiga $ANTIGA"; done
