package br.com.agnellovinheria.model;

import java.math.BigDecimal;

public class ConfigFidelidade {
    private Long vinheriaId;
    private BigDecimal pontosPorReal;
    private Integer validadeDias;
    private String recompensas;

    public ConfigFidelidade() {
    }

    public ConfigFidelidade(Long vinheriaId) {
        this.vinheriaId = vinheriaId;
        this.pontosPorReal = BigDecimal.ONE;
        this.validadeDias = 365;
    }

    public Long getVinheriaId() {
        return vinheriaId;
    }

    public void setVinheriaId(Long vinheriaId) {
        this.vinheriaId = vinheriaId;
    }

    public BigDecimal getPontosPorReal() {
        return pontosPorReal;
    }

    public void setPontosPorReal(BigDecimal pontosPorReal) {
        this.pontosPorReal = pontosPorReal;
    }

    public Integer getValidadeDias() {
        return validadeDias;
    }

    public void setValidadeDias(Integer validadeDias) {
        this.validadeDias = validadeDias;
    }

    public String getRecompensas() {
        return recompensas;
    }

    public void setRecompensas(String recompensas) {
        this.recompensas = recompensas;
    }
}
