package br.com.vinheiro.dao;

import br.com.vinheiro.model.Pagamento;
import br.com.vinheiro.model.enums.MetodoPagamento;
import br.com.vinheiro.model.enums.StatusPagamento;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagamentoDAO {
    private static final Logger LOGGER = Logger.getLogger(PagamentoDAO.class.getName());

    public Optional<Pagamento> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM pagamento WHERE id = ?";
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

    public void save(Pagamento pagamento, Connection conn) throws SQLException {
        String sql = "INSERT INTO pagamento (pedido_id, metodo, status, valor, gateway_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, pagamento.getPedidoId(), Types.BIGINT);
            
            if (pagamento.getMetodo() != null) stmt.setString(2, pagamento.getMetodo().name());
            else stmt.setNull(2, Types.VARCHAR);
            
            if (pagamento.getStatus() != null) stmt.setString(3, pagamento.getStatus().name());
            else stmt.setNull(3, Types.VARCHAR);
            
            stmt.setBigDecimal(4, pagamento.getValor());
            stmt.setString(5, pagamento.getGatewayId());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setId(rs.getLong(1));
                }
            }
        }
    }

    private Pagamento mapRow(ResultSet rs) throws SQLException {
        Pagamento p = new Pagamento();
        p.setId(rs.getLong("id"));
        p.setPedidoId(rs.getLong("pedido_id"));
        
        String metodoStr = rs.getString("metodo");
        if (metodoStr != null) {
            try {
                p.setMetodo(MetodoPagamento.valueOf(metodoStr));
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid MetodoPagamento value: {0} for pagamento id: {1}", new Object[]{metodoStr, p.getId()});
            }
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                p.setStatus(StatusPagamento.valueOf(statusStr));
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid StatusPagamento value: {0} for pagamento id: {1}", new Object[]{statusStr, p.getId()});
            }
        }
        
        p.setValor(rs.getBigDecimal("valor"));
        p.setGatewayId(rs.getString("gateway_id"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            p.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        return p;
    }
}
