#!/usr/bin/env bash
# Captura o tráfego da rede vinheria-net (interface br-vinheria) com tcpdump, em um container, e salva em
# capturas/<nome>.pcap para abrir no Wireshark.
# Útil principalmente no Windows e no macOS (Docker Desktop), onde a br-vinheria fica dentro da VM do Docker
# e não aparece na lista de interfaces do Wireshark. No Linux também é possível capturar direto no Wireshark.
#
# Uso: capturar-trafego.sh <nome> [filtro tcpdump]
#   ./infra/scripts/capturar-trafego.sh login-http
#   ./infra/scripts/capturar-trafego.sh dhcp-dns "port 67 or port 68 or port 53"
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
NOME="${1:?informe o nome do arquivo (sem extensão)}"
FILTRO="${2:-not port 5432}"
mkdir -p "$RAIZ/capturas"

docker rm -f vinheria-captura >/dev/null 2>&1 || true
docker run -d --rm --name vinheria-captura --network host --cap-add NET_RAW --cap-add NET_ADMIN \
    --security-opt label=disable -v "$RAIZ/capturas:/cap" alpine:3.20 \
    sh -c "apk add -q --no-cache tcpdump && exec tcpdump -i br-vinheria -U -w '/cap/$NOME.pcap' $FILTRO" >/dev/null

# Espera o tcpdump começar a escutar antes de liberar o usuário
for _ in $(seq 1 30); do
    docker logs vinheria-captura 2>&1 | grep -q "listening on" && break
    sleep 1
done

echo "Capturando em br-vinheria (filtro: $FILTRO) -> capturas/$NOME.pcap"
echo "Gere o tráfego em outro terminal (ex.: ./infra/scripts/gerar-trafego-demo.sh) e pressione Enter para parar."
read -r _

docker stop vinheria-captura >/dev/null
# O arquivo foi criado pelo root do container: devolve a posse ao usuário atual
docker run --rm --security-opt label=disable -v "$RAIZ/capturas:/cap" alpine:3.20 \
    chown "$(id -u):$(id -g)" "/cap/$NOME.pcap" 2>/dev/null || true
echo "Captura salva em capturas/$NOME.pcap — abra no Wireshark (Arquivo > Abrir)."
