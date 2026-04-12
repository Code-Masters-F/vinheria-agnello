package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.Campanha;
import br.com.agnellovinheria.model.enums.CanalCampanha;
import br.com.agnellovinheria.model.enums.StatusCampanha;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CampanhaDAO {
    private static final Logger LOGGER = Logger.getLogger(CampanhaDAO.class.getName());

    public Optional<Campanha> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM campanha WHERE id = ?";
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

    public void save(Campanha campanha, Connection conn) throws SQLException {
        String sql = "INSERT INTO campanha (vinheria_id, nome, mensagem, filtro_tipo, canal, status, enviada_em) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, campanha.getVinheriaId(), Types.BIGINT);
            stmt.setString(2, campanha.getNome());
            stmt.setString(3, campanha.getMensagem());
            stmt.setString(4, campanha.getFiltroTipo());
            
            if (campanha.getCanal() != null) stmt.setString(5, campanha.getCanal().name());
            else stmt.setNull(5, Types.VARCHAR);
            
            if (campanha.getStatus() != null) stmt.setString(6, campanha.getStatus().name());
            else stmt.setNull(6, Types.VARCHAR);
            
            if (campanha.getEnviadaEm() != null) stmt.setTimestamp(7, Timestamp.valueOf(campanha.getEnviadaEm()));
            else stmt.setNull(7, Types.TIMESTAMP);
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    campanha.setId(rs.getLong(1));
                }
            }
        }
    }

    public void atualizarStatus(Long id, StatusCampanha status, Connection conn) throws SQLException {
        String sql = "UPDATE campanha SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    private Campanha mapRow(ResultSet rs) throws SQLException {
        Campanha c = new Campanha();
        c.setId(rs.getLong("id"));
        c.setVinheriaId(rs.getLong("vinheria_id"));
        c.setNome(rs.getString("nome"));
        c.setMensagem(rs.getString("mensagem"));
        c.setFiltroTipo(rs.getString("filtro_tipo"));
        
        String canalStr = rs.getString("canal");
        if (canalStr != null) {
            try {
                c.setCanal(CanalCampanha.valueOf(canalStr));
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid CanalCampanha value: {0} for campanha id: {1}", new Object[]{canalStr, c.getId()});
            }
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                c.setStatus(StatusCampanha.valueOf(statusStr));
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid StatusCampanha value: {0} for campanha id: {1}", new Object[]{statusStr, c.getId()});
            }
        }
        
        Timestamp enviadaEm = rs.getTimestamp("enviada_em");
        if (enviadaEm != null) {
            c.setEnviadaEm(enviadaEm.toLocalDateTime());
        }
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            c.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        return c;
    }
}
