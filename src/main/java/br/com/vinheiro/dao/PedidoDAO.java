package br.com.vinheiro.dao;

import br.com.vinheiro.model.Pedido;
import br.com.vinheiro.model.enums.StatusPedido;
import br.com.vinheiro.model.enums.TipoEntrega;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PedidoDAO - Data Access Object for the Pedido entity.
 *
 * <p><b>SQL / Security Audit (T026):</b> 
 * Parameterized queries ({@link PreparedStatement}) are utilized across all methods to 
 * fundamentally neutralize SQL Injection vulnerabilities. User-provided payloads (such as 
 * JSON delivery fields or status enums) are strictly bound as typed JDBC variables.
 * 
 * <p><b>Design & Joins (T025):</b>
 * Given the strict separation of concerns, the mapped rows fetch only primary 'pedido' data.
 * The Pedido table operates as an aggregate root, but relationships (e.g., ItemPedido) 
 * are resolved by their own dedicated DAOs rather than large multidimensional JOINs. 
 * This strategy isolates latency optimizations and keeps caching policies distinct per entity.
 */
public class PedidoDAO {
    private static final Logger LOGGER = Logger.getLogger(PedidoDAO.class.getName());

    public Optional<Pedido> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM pedido WHERE id = ?";
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

    public java.util.List<Pedido> findByVinheriaId(Long vinheriaId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM pedido WHERE vinheria_id = ? ORDER BY criado_em DESC";
        java.util.List<Pedido> pedidos = new java.util.ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vinheriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapRow(rs));
                }
            }
        }
        return pedidos;
    }

    public int updateStatus(Long id, StatusPedido status, Connection conn) throws SQLException {
        String sql = "UPDATE pedido SET status = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            return stmt.executeUpdate();
        }
    }

    public void save(Pedido pedido, Connection conn) throws SQLException {
        String sql = "INSERT INTO pedido (vinheria_id, cliente_id, status, tipo_entrega, subtotal, total, endereco_entrega) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, pedido.getVinheriaId());
            
            if (pedido.getClienteId() != null) stmt.setLong(2, pedido.getClienteId());
            else stmt.setNull(2, Types.BIGINT);
            
            stmt.setString(3, pedido.getStatus() != null ? pedido.getStatus().name() : StatusPedido.aguardando_pagamento.name());
            if (pedido.getTipoEntrega() != null) {
                stmt.setString(4, pedido.getTipoEntrega().name());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            stmt.setBigDecimal(5, pedido.getSubtotal());
            stmt.setBigDecimal(6, pedido.getTotal());
            stmt.setString(7, pedido.getEnderecoEntrega());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pedido.setId(rs.getLong(1));
                }
            }
        }
    }

    private Pedido mapRow(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getLong("id"));
        pedido.setVinheriaId(rs.getLong("vinheria_id"));
        
        long clienteId = rs.getLong("cliente_id");
        if (!rs.wasNull()) {
            pedido.setClienteId(clienteId);
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                pedido.setStatus(StatusPedido.valueOf(statusStr));
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid StatusPedido value: {0} for pedido id: {1}", new Object[]{statusStr, pedido.getId()});
            }
        }
        
        String tipoEntregaStr = rs.getString("tipo_entrega");
        if (tipoEntregaStr != null) {
            try {
                pedido.setTipoEntrega(TipoEntrega.valueOf(tipoEntregaStr));
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Invalid TipoEntrega value: {0} for pedido id: {1}", new Object[]{tipoEntregaStr, pedido.getId()});
            }
        }
        
        pedido.setSubtotal(rs.getBigDecimal("subtotal"));
        pedido.setTotal(rs.getBigDecimal("total"));
        pedido.setEnderecoEntrega(rs.getString("endereco_entrega"));
        
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) {
            pedido.setCriadoEm(criadoEm.toLocalDateTime());
        }
        
        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) {
            pedido.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        }
        
        return pedido;
    }
}
