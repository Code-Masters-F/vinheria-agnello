package br.com.vinheiro.dao;

import br.com.vinheiro.model.ConfigFidelidade;

import java.sql.*;
import java.util.Optional;

/**
 * ConfigFidelidadeDAO - Data Access Object for configuration strategies.
 *
 * <p><b>SQL / Security Audit (T026):</b> 
 * Data flow is strictly regulated through {@link PreparedStatement} setters (e.g., setBigDecimal).
 * There is zero dynamic SQL concatenation, rendering the class impervious to first 
 * and second order SQL injection vectors.
 * 
 * <p><b>Design & Joins (T025):</b>
 * This entity operates strictly in a 1:1 relationship with the Vinheria table, acting as a functional 
 * extension of the core platform configuration. Due to the high-read/low-write characteristic of configurations, 
 * queries are highly isolated, and JOINs are entirely bypassed in favor of direct primary key exact-matches.
 */
public class ConfigFidelidadeDAO {

    public Optional<ConfigFidelidade> findByVinheriaId(Long vinheriaId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM config_fidelidade WHERE vinheria_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vinheriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void save(ConfigFidelidade config, Connection conn) throws SQLException {
        // Assume upsert or just insert since it shares PK with Vinheria

        // Emulando ON DUPLICATE KEY pra usar no H2/MySQL
        
        // Let's use standard insert, and a separate update if needed, but MERGE works in H2, OR we can check if exists.
        Optional<ConfigFidelidade> existing = findByVinheriaId(config.getVinheriaId(), conn);
        if (existing.isPresent()) {
            update(config, conn);
        } else {
            insert(config, conn);
        }
    }
    
    private void insert(ConfigFidelidade config, Connection conn) throws SQLException {
        String sql = "INSERT INTO config_fidelidade (vinheria_id, pontos_por_real, validade_dias, recompensas) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, config.getVinheriaId());
            stmt.setBigDecimal(2, config.getPontosPorReal());
            if (config.getValidadeDias() != null) stmt.setInt(3, config.getValidadeDias());
            else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, config.getRecompensas());
            stmt.executeUpdate();
        }
    }
    
    private void update(ConfigFidelidade config, Connection conn) throws SQLException {
        String sql = "UPDATE config_fidelidade SET pontos_por_real = ?, validade_dias = ?, recompensas = ? WHERE vinheria_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, config.getPontosPorReal());
            if (config.getValidadeDias() != null) stmt.setInt(2, config.getValidadeDias());
            else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, config.getRecompensas());
            stmt.setLong(4, config.getVinheriaId());
            stmt.executeUpdate();
        }
    }

    private ConfigFidelidade mapRow(ResultSet rs) throws SQLException {
        ConfigFidelidade c = new ConfigFidelidade();
        c.setVinheriaId(rs.getLong("vinheria_id"));
        c.setPontosPorReal(rs.getBigDecimal("pontos_por_real"));
        
        int dias = rs.getInt("validade_dias");
        if (!rs.wasNull()) {
            c.setValidadeDias(dias);
        }
        
        c.setRecompensas(rs.getString("recompensas"));
        return c;
    }
}
