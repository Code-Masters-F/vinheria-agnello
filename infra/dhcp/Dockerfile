# Container de rede de cada serviço: é o dono da interface (o serviço .NET entra nela com
# network_mode: service:<nome>-rede), obtém o IP no dnsmasq por DHCP e mantém a concessão renovada.
# Só este container recebe NET_ADMIN; o serviço .NET continua sem privilégios (menor privilégio).
FROM alpine:3.20

COPY udhcpc.script /usr/local/bin/udhcpc-vinheria
RUN chmod 755 /usr/local/bin/udhcpc-vinheria

# -f: fica em primeiro plano renovando a concessão | -R: libera o IP ao parar
# -t/-T/-A: tentativas, intervalo e espera após falha
# O hostname (opção 12) é informado no compose: command: ["-x", "hostname:<serviço>"]
ENTRYPOINT ["udhcpc", "-i", "eth0", "-f", "-R", "-s", "/usr/local/bin/udhcpc-vinheria", "-t", "10", "-T", "2", "-A", "5"]
