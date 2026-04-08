package br.com.vinheiro.dao;

import br.com.vinheiro.model.HistoricoPontos;

import java.sql.*;
import java.util.Optional;

public class HistoricoPontosDAO {

    public Optional<HistoricoPontos> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM historico_pontos WHERE id = ?";
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

    public void save(HistoricoPontos historico, Connection conn) throws SQLException {
        String sql = "INSERT INTO historico_pontos (cliente_id, pedido_id, pontos, descricao) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, historico.getClienteId(), Types.BIGINT);
            
            if (historico.getPedidoId() != null) stmt.setLong(2, historico.getPedidoId());
            else stmt.setNull(2, Types.BIGINT);
            
            if (historico.getPontos() != null) stmt.setInt(3, historico.getPontos());
            else stmt.setNull(3, Types.INTEGER);
            
            stmt.setString(4, historico.getDescricao());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    historico.setId(rs.getLong(1));
                }
            }
        }
    }

    private HistoricoPontos mapRow(ResultSet rs) throws SQLException {
        HistoricoPontos h = new HistoricoPontos();
        h.setId(rs.getLong("id"));
        h.setClienteId(rs.getLong("cliente_id"));
        
        long pedidoId = rs.getLong("pedido_id");
        if (!rs.wasNull()) {
            h.setPedidoId(pedidoId);
        }
        
        h.setPontos(rs.getInt("pontos"));
        h.setDescricao(rs.getString("descricao"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            h.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        return h;
    }
}
