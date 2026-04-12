package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.ScanQRCode;

import java.sql.*;
import java.util.Optional;

public class ScanQRCodeDAO {

    public Optional<ScanQRCode> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM scan_qrcode WHERE id = ?";
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

    public void save(ScanQRCode scan, Connection conn) throws SQLException {
        String sql = "INSERT INTO scan_qrcode (vinheria_id, ocasiao, faixa_preco, converteu) VALUES (?, ?, ?, ?)";
        if (scan.getVinheriaId() == null) {
            throw new IllegalArgumentException("vinheriaId must not be null");
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, scan.getVinheriaId());
            stmt.setString(2, scan.getOcasiao());
            stmt.setString(3, scan.getFaixaPreco());
            stmt.setBoolean(4, scan.isConverteu());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    scan.setId(rs.getLong(1));
                }
            }
        }
    }

    private ScanQRCode mapRow(ResultSet rs) throws SQLException {
        ScanQRCode s = new ScanQRCode();
        s.setId(rs.getLong("id"));
        s.setVinheriaId(rs.getLong("vinheria_id"));
        s.setOcasiao(rs.getString("ocasiao"));
        s.setFaixaPreco(rs.getString("faixa_preco"));
        s.setConverteu(rs.getBoolean("converteu"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            s.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        return s;
    }
}
