package br.com.vinheiro.service;

import br.com.vinheiro.dao.VinheriaDAO;
import br.com.vinheiro.model.Vinheria;

import java.util.List;
import java.util.Optional;

public class VinheriaService {

    private final VinheriaDAO dao;

    public VinheriaService() {
        this.dao = new VinheriaDAO();
    }

    public Optional<Vinheria> findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return dao.findBySlug(slug);
    }

    public Optional<Vinheria> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return dao.findById(id);
    }

    public List<Vinheria> findAll() {
        return dao.findAll();
    }

    public List<Vinheria> findAllIncludingInactive() {
        return dao.findAllIncludingInactive();
    }

    public Long save(Vinheria vinheria) {
        validarVinheria(vinheria);

        if (dao.existsBySlug(vinheria.getSlug())) {
            throw new IllegalArgumentException("Slug já existe: " + vinheria.getSlug());
        }

        return dao.save(vinheria);
    }

    public void update(Vinheria vinheria) {
        if (vinheria.getId() == null) {
            throw new IllegalArgumentException("ID da vinheria é obrigatório para atualização");
        }

        validarVinheria(vinheria);

        if (dao.existsBySlugAndNotId(vinheria.getSlug(), vinheria.getId())) {
            throw new IllegalArgumentException("Slug já está em uso: " + vinheria.getSlug());
        }

        dao.update(vinheria);
    }

    public void ativar(Long id) {
        Optional<Vinheria> optVinheria = findById(id);
        if (optVinheria.isEmpty()) {
            throw new IllegalArgumentException("Vinheria não encontrada: " + id);
        }

        Vinheria vinheria = optVinheria.get();
        vinheria.setAtivo(true);
        dao.update(vinheria);
    }

    public void desativar(Long id) {
        Optional<Vinheria> optVinheria = findById(id);
        if (optVinheria.isEmpty()) {
            throw new IllegalArgumentException("Vinheria não encontrada: " + id);
        }

        Vinheria vinheria = optVinheria.get();
        vinheria.setAtivo(false);
        dao.update(vinheria);
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        dao.delete(id);
    }

    private void validarVinheria(Vinheria vinheria) {
        if (vinheria == null) {
            throw new IllegalArgumentException("Vinheria não pode ser nula");
        }
        if (vinheria.getNome() == null || vinheria.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (vinheria.getSlug() == null || vinheria.getSlug().isBlank()) {
            throw new IllegalArgumentException("Slug é obrigatório");
        }
        if (!isValidSlug(vinheria.getSlug())) {
            throw new IllegalArgumentException("Slug inválido. Use apenas letras minúsculas, números e hifens");
        }
    }

    public boolean isValidSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }
        return slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$");
    }

    public String generateSlug(String nome) {
        if (nome == null || nome.isBlank()) {
            return "";
        }
        return nome.toLowerCase()
                .normalize("NFD")
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
