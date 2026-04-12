package br.com.agnellovinheria.model;

import br.com.agnellovinheria.model.enums.CanalCampanha;
import br.com.agnellovinheria.model.enums.StatusCampanha;

import java.time.LocalDateTime;

public class Campanha {
    private Long id;
    private Long vinheriaId;
    private String nome;
    private String mensagem;
    private String filtroTipo;
    private CanalCampanha canal;
    private StatusCampanha status;
    private LocalDateTime enviadaEm;
    private LocalDateTime criadoEm;

    public Campanha() {
    }

    public Campanha(Long id, String nome, String filtroTipo) {
        this.id = id;
        this.nome = nome;
        this.filtroTipo = filtroTipo;
        this.status = StatusCampanha.rascunho;
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getFiltroTipo() {
        return filtroTipo;
    }

    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    public CanalCampanha getCanal() {
        return canal;
    }

    public void setCanal(CanalCampanha canal) {
        this.canal = canal;
    }

    public StatusCampanha getStatus() {
        return status;
    }

    public void setStatus(StatusCampanha status) {
        this.status = status;
    }

    public LocalDateTime getEnviadaEm() {
        return enviadaEm;
    }

    public void setEnviadaEm(LocalDateTime enviadaEm) {
        this.enviadaEm = enviadaEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
