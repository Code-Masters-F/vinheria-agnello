package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.AvaliacaoVinho;

import java.sql.*;
import java.util.Optional;

public class AvaliacaoVinhoDAO {

    public Optional<AvaliacaoVinho> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM avaliacao_vinho WHERE id = ?";
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

    public void save(AvaliacaoVinho avaliacao, Connection conn) throws SQLException {
        String sql = "INSERT INTO avaliacao_vinho (cliente_id, vinho_id, nota, ocasiao) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, avaliacao.getClienteId(), Types.BIGINT);
            stmt.setObject(2, avaliacao.getVinhoId(), Types.BIGINT);
            
            if (avaliacao.getNota() != null) stmt.setInt(3, avaliacao.getNota());
            else stmt.setNull(3, Types.INTEGER);
            
            stmt.setString(4, avaliacao.getOcasiao());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    avaliacao.setId(rs.getLong(1));
                }
            }
        }
    }

    private AvaliacaoVinho mapRow(ResultSet rs) throws SQLException {
        AvaliacaoVinho p = new AvaliacaoVinho();
        p.setId(rs.getLong("id"));
        p.setClienteId(rs.getLong("cliente_id"));
        p.setVinhoId(rs.getLong("vinho_id"));
        
        int nota = rs.getInt("nota");
        if (!rs.wasNull()) {
            p.setNota(nota);
        }
        
        p.setOcasiao(rs.getString("ocasiao"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            p.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        return p;
    }
}
