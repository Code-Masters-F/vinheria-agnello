package br.com.vinheiro.model;

import java.time.LocalDateTime;

public class ScanQRCode {
    private Long id;
    private Long vinheriaId;
    private String ocasiao;
    private String faixaPreco;
    private boolean converteu;
    private LocalDateTime criadoEm;

    public ScanQRCode() {
    }

    public ScanQRCode(Long id, Long vinheriaId, String ocasiao) {
        this.id = id;
        this.vinheriaId = vinheriaId;
        this.ocasiao = ocasiao;
        this.converteu = false;
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

    public String getOcasiao() {
        return ocasiao;
    }

    public void setOcasiao(String ocasiao) {
        this.ocasiao = ocasiao;
    }

    public String getFaixaPreco() {
        return faixaPreco;
    }

    public void setFaixaPreco(String faixaPreco) {
        this.faixaPreco = faixaPreco;
    }

    public boolean isConverteu() {
        return converteu;
    }

    public void setConverteu(boolean converteu) {
        this.converteu = converteu;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
