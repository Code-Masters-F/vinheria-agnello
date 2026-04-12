package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.ItemPedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemPedidoDAO {

    public Optional<ItemPedido> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM item_pedido WHERE id = ?";
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

    public List<ItemPedido> findByPedidoId(Long pedidoId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM item_pedido WHERE pedido_id = ?";
        List<ItemPedido> itens = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pedidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(mapRow(rs));
                }
            }
        }
        return itens;
    }

    public void save(ItemPedido item, Connection conn) throws SQLException {
        String sql = "INSERT INTO item_pedido (pedido_id, vinho_id, quantidade, preco_unit) VALUES (?, ?, ?, ?)";
        if (item.getPedidoId() == null || item.getVinhoId() == null || item.getQuantidade() == null) {
            throw new IllegalArgumentException("Pedido ID, Vinho ID and Quantidade must not be null");
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, item.getPedidoId());
            stmt.setLong(2, item.getVinhoId());
            stmt.setInt(3, item.getQuantidade());
            stmt.setBigDecimal(4, item.getPrecoUnit());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }
        }
    }

    private ItemPedido mapRow(ResultSet rs) throws SQLException {
        ItemPedido item = new ItemPedido();
        item.setId(rs.getLong("id"));
        item.setPedidoId(rs.getLong("pedido_id"));
        item.setVinhoId(rs.getLong("vinho_id"));
        item.setQuantidade(rs.getInt("quantidade"));
        item.setPrecoUnit(rs.getBigDecimal("preco_unit"));
        return item;
    }
}
