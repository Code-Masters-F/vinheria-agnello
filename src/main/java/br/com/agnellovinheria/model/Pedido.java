package br.com.agnellovinheria.model;

import br.com.agnellovinheria.model.enums.StatusPedido;
import br.com.agnellovinheria.model.enums.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedido {
    private Long id;
    private Long vinheriaId;
    private Long clienteId;
    private StatusPedido status;
    private TipoEntrega tipoEntrega;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String enderecoEntrega;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Pedido() {
    }

    public Pedido(Long id, Long vinheriaId, Long clienteId, BigDecimal total) {
        this.id = id;
        this.vinheriaId = vinheriaId;
        this.clienteId = clienteId;
        this.total = total;
        this.status = StatusPedido.aguardando_pagamento;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVinheriaId() {
        return vinheriaId;
    }

    public void setVinheriaId(Long vinheriaId) {
        this.vinheriaId = vinheriaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(TipoEntrega tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
