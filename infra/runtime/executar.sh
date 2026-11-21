#!/bin/sh
# =====================================================================
# Servidor de aplicação dos microsserviços (simula o IIS)
# =====================================================================
#
# Executa a versão publicada pelo pipeline em /publicacao/atual (link para releases/<build>).
# Quando um novo deploy troca o link "atual", o processo é reciclado para a nova versão, como a
# reciclagem do pool de aplicativos do IIS ao detectar um novo deploy. A pasta é montada somente
# leitura: o serviço não consegue alterar o próprio código.
#
# Uso: executar.sh <Arquivo.dll>
set -u

DLL="${1:?informe a DLL do serviço, ex.: IdentidadeApi.dll}"
PASTA=/publicacao
INTERVALO=3
PID=""

versao_publicada() { readlink -f "$PASTA/atual" 2>/dev/null; }
log() { echo "[servidor] $*"; }

# docker stop: repassa o sinal para o processo .NET e encerra de forma limpa
encerrar() {
    if [ -n "$PID" ]; then kill -TERM "$PID" 2>/dev/null; wait "$PID" 2>/dev/null; fi
    exit 0
}
trap encerrar TERM INT

until [ -f "$PASTA/atual/$DLL" ]; do
    log "aguardando o primeiro deploy em $PASTA/atual ..."
    sleep 5 & wait $!
done

while true; do
    VERSAO_PASTA=$(versao_publicada)
    VERSAO=$(basename "$VERSAO_PASTA")
    log "iniciando $DLL versão $VERSAO"

    # APP_VERSAO é exposta pelo serviço em GET /versao (usada na verificação pós-deploy)
    (cd "$VERSAO_PASTA" && APP_VERSAO="$VERSAO" exec dotnet "$DLL") &
    PID=$!

    while kill -0 "$PID" 2>/dev/null; do
        sleep "$INTERVALO" & wait $!
        NOVA=$(versao_publicada)
        if [ -n "$NOVA" ] && [ "$NOVA" != "$VERSAO_PASTA" ] && [ -f "$NOVA/$DLL" ]; then
            log "novo deploy detectado: versão $VERSAO -> $(basename "$NOVA"); reciclando o processo"
            kill -TERM "$PID" 2>/dev/null
            wait "$PID" 2>/dev/null
            break
        fi
    done

    # O processo terminou sem um novo deploy (falha): encerra o container e deixa o Docker reiniciar.
    if [ "$(versao_publicada)" = "$VERSAO_PASTA" ]; then
        wait "$PID"; CODIGO=$?
        log "o processo terminou (código $CODIGO)"
        exit "$CODIGO"
    fi
done
