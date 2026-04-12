package br.com.agnellovinheria.model;

public class Recomendacao {
    private Long id;
    private Long clienteId;
    private String ocasiao;
    private String estilo;
    private String faixaPreco;

    public Recomendacao() {
    }

    public Recomendacao(String ocasiao, String estilo, String faixaPreco) {
        this.ocasiao = ocasiao;
        this.estilo = estilo;
        this.faixaPreco = faixaPreco;
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

    public String getOcasiao() {
        return ocasiao;
    }

    public void setOcasiao(String ocasiao) {
        this.ocasiao = ocasiao;
    }

    public String getEstilo() {
        return estilo;
    }

    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    public String getFaixaPreco() {
        return faixaPreco;
    }

    public void setFaixaPreco(String faixaPreco) {
        this.faixaPreco = faixaPreco;
    }
}
