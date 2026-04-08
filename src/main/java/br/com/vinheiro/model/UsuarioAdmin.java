package br.com.vinheiro.model;

import java.time.LocalDateTime;

public class UsuarioAdmin {
    private long id;
    private Vinheria vinheria;
    private String nome;
    private String email;
    private String senhaHash;
    private LocalDateTime criadoEm;

    public UsuarioAdmin() {}

    public UsuarioAdmin(long id, Vinheria vinheria,  String nome, String senhaHash) {
        this.id = id;
        this.vinheria = vinheria;
        this.nome = nome;
        this.senhaHash = senhaHash;
        this.criadoEm = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
