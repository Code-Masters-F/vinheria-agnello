package br.com.vinheiro.model;

import br.com.vinheiro.model.enums.MetodoPagamento;
import br.com.vinheiro.model.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pagamento {
    private Long id;
    private Long pedidoId;
    private MetodoPagamento metodo;
    private StatusPagamento status;
    private BigDecimal valor;
    private String gatewayId;
    private LocalDateTime criadoEm;

    public Pagamento() {
    }

    public Pagamento(Long id, Long pedidoId, BigDecimal valor) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.valor = valor;
        this.status = StatusPagamento.pendente;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPagamento metodo) {
        this.metodo = metodo;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
