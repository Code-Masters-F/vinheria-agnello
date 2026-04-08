package br.com.vinheiro.service;

import br.com.vinheiro.config.DatabaseConfig;
import br.com.vinheiro.dao.ItemPedidoDAO;
import br.com.vinheiro.dao.PedidoDAO;
import br.com.vinheiro.dao.VinhoDAO;
import br.com.vinheiro.model.ItemPedido;
import br.com.vinheiro.model.Pedido;
import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.service.exceptions.InsufficientStockException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PedidoService {
    private final PedidoDAO pedidoDAO;
    private final ItemPedidoDAO itemPedidoDAO;
    private final VinhoDAO vinhoDAO;

    public PedidoService() {
        this.pedidoDAO = new PedidoDAO();
        this.itemPedidoDAO = new ItemPedidoDAO();
        this.vinhoDAO = new VinhoDAO();
    }

    public PedidoService(PedidoDAO pedidoDAO, ItemPedidoDAO itemPedidoDAO, VinhoDAO vinhoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.itemPedidoDAO = itemPedidoDAO;
        this.vinhoDAO = vinhoDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public Pedido criarPedido(Pedido pedido, List<ItemPedido> itens) throws InsufficientStockException, Exception {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Em um cenário real de lock pessimista absoluto:
                // O DAO deveria ter um findByIdForUpdate, mas como estritamente utilizamos findById, 
                // assumimos a transação como boundary de lock.
                for (ItemPedido item : itens) {
                    Optional<Vinho> ov = vinhoDAO.findById(item.getVinhoId(), conn);
                    if (ov.isEmpty()) {
                        throw new Exception("Vinho " + item.getVinhoId() + " não encontrado.");
                    }
                    Vinho v = ov.get();
                    if (v.getEstoque() < item.getQuantidade()) {
                        throw new InsufficientStockException("Estoque insuficiente para o vinho: " + v.getNome());
                    }
                    // Decrementa o estoque logicamente (o checkout já reserva)
                    vinhoDAO.updateEstoque(v.getId(), v.getEstoque() - item.getQuantidade(), conn);
                }

                pedidoDAO.save(pedido, conn);
                
                for (ItemPedido item : itens) {
                    item.setPedidoId(pedido.getId());
                    itemPedidoDAO.save(item, conn);
                }
                
                conn.commit();
                return pedido;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
