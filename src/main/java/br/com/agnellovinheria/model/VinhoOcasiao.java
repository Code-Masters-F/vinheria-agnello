package br.com.agnellovinheria.model;

import br.com.agnellovinheria.model.enums.Ocasiao;

public class VinhoOcasiao {
    private Long vinhoId;
    private Ocasiao ocasiao;

    public VinhoOcasiao() {
    }

    public VinhoOcasiao(Long vinhoId, Ocasiao ocasiao) {
        this.vinhoId = vinhoId;
        this.ocasiao = ocasiao;
    }

    public Long getVinhoId() {
        return vinhoId;
    }

    public void setVinhoId(Long vinhoId) {
        this.vinhoId = vinhoId;
    }

    public Ocasiao getOcasiao() {
        return ocasiao;
    }

    public void setOcasiao(Ocasiao ocasiao) {
        this.ocasiao = ocasiao;
    }
}
