package br.com.vinheiro.dao;

import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VinheriaDAO {

    public Optional<Vinheria> findBySlug(String slug) {
        String sql = "SELECT * FROM vinheria WHERE slug = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, slug);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVinheria(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar vinheria por slug: " + slug, e);
        }

        return Optional.empty();
    }

    public Optional<Vinheria> findBySlugActive(String slug) {
        String sql = "SELECT * FROM vinheria WHERE slug = ? AND ativo = true";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, slug);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVinheria(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar vinheria ativa por slug: " + slug, e);
        }

        return Optional.empty();
    }

    public Optional<Vinheria> findById(Long id) {
        String sql = "SELECT * FROM vinheria WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVinheria(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar vinheria por ID: " + id, e);
        }

        return Optional.empty();
    }

    public List<Vinheria> findAll() {
        String sql = "SELECT * FROM vinheria WHERE ativo = true ORDER BY nome";
        List<Vinheria> vinherias = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vinherias.add(mapResultSetToVinheria(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar vinherias", e);
        }

        return vinherias;
    }

    public List<Vinheria> findAllIncludingInactive() {
        String sql = "SELECT * FROM vinheria ORDER BY nome";
        List<Vinheria> vinherias = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                vinherias.add(mapResultSetToVinheria(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todas as vinherias", e);
        }

        return vinherias;
    }

    public Long save(Vinheria vinheria) {
        String sql = "INSERT INTO vinheria (nome, slug, logo_url, cor_primaria, cor_secundaria, ativo, criado_em) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vinheria.getNome());
            stmt.setString(2, vinheria.getSlug());
            stmt.setString(3, vinheria.getLogoUrl());
            stmt.setString(4, vinheria.getCorPrimaria());
            stmt.setString(5, vinheria.getCorSecundaria());
            stmt.setBoolean(6, vinheria.isAtivo());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar vinheria", e);
        }

        throw new RuntimeException("Erro ao obter ID gerado");
    }

    public void update(Vinheria vinheria) {
        String sql = "UPDATE vinheria SET nome = ?, slug = ?, logo_url = ?, cor_primaria = ?, " +
                "cor_secundaria = ?, ativo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vinheria.getNome());
            stmt.setString(2, vinheria.getSlug());
            stmt.setString(3, vinheria.getLogoUrl());
            stmt.setString(4, vinheria.getCorPrimaria());
            stmt.setString(5, vinheria.getCorSecundaria());
            stmt.setBoolean(6, vinheria.isAtivo());
            stmt.setLong(7, vinheria.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar vinheria: " + vinheria.getId(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM vinheria WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar vinheria: " + id, e);
        }
    }

    public boolean existsBySlug(String slug) {
        String sql = "SELECT COUNT(*) FROM vinheria WHERE slug = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, slug);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar slug: " + slug, e);
        }

        return false;
    }

    public boolean existsBySlugAndNotId(String slug, Long id) {
        String sql = "SELECT COUNT(*) FROM vinheria WHERE slug = ? AND id != ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, slug);
            stmt.setLong(2, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar slug: " + slug, e);
        }

        return false;
    }

    private Vinheria mapResultSetToVinheria(ResultSet rs) throws SQLException {
        Vinheria vinheria = new Vinheria();
        vinheria.setId(rs.getLong("id"));
        vinheria.setNome(rs.getString("nome"));
        vinheria.setSlug(rs.getString("slug"));
        vinheria.setLogoUrl(rs.getString("logo_url"));
        vinheria.setCorPrimaria(rs.getString("cor_primaria"));
        vinheria.setCorSecundaria(rs.getString("cor_secundaria"));
        vinheria.setAtivo(rs.getBoolean("ativo"));

        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            vinheria.setCriadoEm(criadoEm.toLocalDateTime());
        }

        return vinheria;
    }
}
