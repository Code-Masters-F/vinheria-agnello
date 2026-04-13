package br.com.agnellovinheria.model;

import br.com.agnellovinheria.model.enums.TipoVinho;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Vinho {
    private Long id;
    private Vinheria vinheria;
    private String nome;
    private TipoVinho tipo;
    private String uva;
    private String pais;
    private String regiao;
    private String safra;
    private String preco;
    private String descricao;
    private String fotoUrl;
    private int estoque;
    private int estoqueMinimo;
    private boolean ativo;
    private Timestamp criadoEm;

    public Vinho() {}

    public Vinho(Long id, Vinheria vinheria, String nome, TipoVinho tipo, String uva, String pais, String regiao, String safra, String preco, String descricao, String fotoUrl, int estoque, int estoqueMinimo, boolean ativo, Timestamp criadoEm) {
        this.id = id;
        this.vinheria = vinheria;
        this.nome = nome;
        this.tipo = tipo;
        this.uva = uva;
        this.pais = pais;
        this.regiao = regiao;
        this.safra = safra;
        this.preco = preco;
        this.descricao = descricao;
        this.fotoUrl = fotoUrl;
        this.estoque = estoque;
        this.estoqueMinimo = estoqueMinimo;
        this.ativo = ativo;
        this.criadoEm = (criadoEm != null) ? criadoEm : Timestamp.valueOf(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vinheria getVinheria() {
        return vinheria;
    }

    public void setVinheria(Vinheria vinheria) {
        this.vinheria = vinheria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoVinho getTipo() {
        return tipo;
    }

    public void setTipo(TipoVinho tipo) {
        this.tipo = tipo;
    }

    public String getUva() {
        return uva;
    }

    public void setUva(String uva) {
        this.uva = uva;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public String getSafra() {
        return safra;
    }

    public void setSafra(String safra) {
        this.safra = safra;
    }

    public String getPreco() {
        return preco;
    }

    public void setPreco(String preco) {
        this.preco = preco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Timestamp getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Timestamp criadoEm) {
        this.criadoEm = criadoEm;
    }
}
