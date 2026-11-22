# Servidor DHCP + DNS da rede vinheria-net
FROM alpine:3.20

RUN apk add --no-cache dnsmasq \
 && mkdir -p /var/lib/misc

COPY dnsmasq.conf /etc/dnsmasq.conf

EXPOSE 53/udp 53/tcp 67/udp

ENTRYPOINT ["dnsmasq", "--keep-in-foreground", "--conf-file=/etc/dnsmasq.conf"]
