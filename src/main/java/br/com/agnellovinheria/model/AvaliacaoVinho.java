package br.com.agnellovinheria.model;

import java.time.LocalDateTime;

public class AvaliacaoVinho {
    private Long id;
    private Long clienteId;
    private Long vinhoId;
    private Integer nota;
    private String ocasiao;
    private LocalDateTime criadoEm;

    public AvaliacaoVinho() {
    }

    public AvaliacaoVinho(Long id, Long clienteId, Long vinhoId) {
        this.id = id;
        this.clienteId = clienteId;
        this.vinhoId = vinhoId;
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

    public Long getVinhoId() {
        return vinhoId;
    }

    public void setVinhoId(Long vinhoId) {
        this.vinhoId = vinhoId;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getOcasiao() {
        return ocasiao;
    }

    public void setOcasiao(String ocasiao) {
        this.ocasiao = ocasiao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
