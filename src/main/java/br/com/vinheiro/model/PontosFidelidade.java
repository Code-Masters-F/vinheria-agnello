package br.com.vinheiro.model;

import java.time.LocalDateTime;

public class PontosFidelidade {
    private Long id;
    private Long clienteId;
    private Integer pontos;
    private String descricao;
    private LocalDateTime criadoEm;

    public PontosFidelidade() {
    }

    public PontosFidelidade(Long id, Long clienteId, Integer pontos) {
        this.id = id;
        this.clienteId = clienteId;
        this.pontos = pontos;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getPontos() {
        return pontos;
    }

    public void setPontos(Integer pontos) {
        this.pontos = pontos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
