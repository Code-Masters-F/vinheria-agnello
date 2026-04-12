package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;

import java.sql.*;
import java.util.Optional;

public class UsuarioAdminDAO {
    
    /**
     * Finds a UsuarioAdmin by its ID.
     */
    public Optional<UsuarioAdmin> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM usuario_admin WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Finds a UsuarioAdmin by email.
     */
    public Optional<UsuarioAdmin> findByEmail(String email, Connection conn) throws SQLException {
        String sql = "SELECT * FROM usuario_admin WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Saves a new UsuarioAdmin to the database.
     */
    public void save(UsuarioAdmin admin, Connection conn) throws SQLException {
        if (admin.getVinheria() == null || admin.getVinheria().getId() == null) {
            throw new IllegalArgumentException("Vinheria and Vinheria ID must not be null");
        }
        String sql = "INSERT INTO usuario_admin (vinheria_id, nome, email, senha_hash, criado_em) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, admin.getVinheria().getId());
            stmt.setString(2, admin.getNome());
            stmt.setString(3, admin.getEmail());
            stmt.setString(4, admin.getSenhaHash());
            
            if (admin.getCriadoEm() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(admin.getCriadoEm()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    admin.setId(rs.getLong(1));
                }
            }
        }
    }

    /**
     * Maps a database row to a UsuarioAdmin instance.
     * Note: The Vinheria instance associated with the admin is a stub with only the ID populated.
     */
    private UsuarioAdmin mapRow(ResultSet rs) throws SQLException {
        UsuarioAdmin admin = new UsuarioAdmin();
        admin.setId(rs.getLong("id"));
        
        Vinheria v = new Vinheria();
        v.setId(rs.getLong("vinheria_id"));
        admin.setVinheria(v);
        
        admin.setNome(rs.getString("nome"));
        admin.setEmail(rs.getString("email"));
        admin.setSenhaHash(rs.getString("senha_hash"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            admin.setCriadoEm(criadoEm.toLocalDateTime());
        }
        return admin;
    }
}
