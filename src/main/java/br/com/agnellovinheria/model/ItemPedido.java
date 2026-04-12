package br.com.agnellovinheria.model;

import java.math.BigDecimal;

public class ItemPedido {
    private Long id;
    private Long pedidoId;
    private Long vinhoId;
    private Integer quantidade;
    private BigDecimal precoUnit;

    public ItemPedido() {
    }

    public ItemPedido(Long id, Long pedidoId, Long vinhoId, Integer quantidade, BigDecimal precoUnit) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.vinhoId = vinhoId;
        this.quantidade = quantidade;
        this.precoUnit = precoUnit;
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

    public Long getVinhoId() {
        return vinhoId;
    }

    public void setVinhoId(Long vinhoId) {
        this.vinhoId = vinhoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnit() {
        return precoUnit;
    }

    public void setPrecoUnit(BigDecimal precoUnit) {
        this.precoUnit = precoUnit;
    }
}
