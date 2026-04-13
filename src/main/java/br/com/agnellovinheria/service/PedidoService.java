package br.com.agnellovinheria.service;

import br.com.agnellovinheria.config.DatabaseConfig;
import br.com.agnellovinheria.dao.ItemPedidoDAO;
import br.com.agnellovinheria.dao.PedidoDAO;
import br.com.agnellovinheria.dao.VinhoDAO;
import br.com.agnellovinheria.model.ItemPedido;
import br.com.agnellovinheria.model.Pedido;
import br.com.agnellovinheria.model.PedidoResumo;
import br.com.agnellovinheria.model.Vinho;
import br.com.agnellovinheria.service.exceptions.InsufficientStockException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PedidoService {
    private static final Logger LOGGER = Logger.getLogger(PedidoService.class.getName());

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

    public List<Pedido> listarPorVinheria(Long vinheriaId) throws SQLException {
        try (Connection conn = getConnection()) {
            return pedidoDAO.findByVinheriaId(vinheriaId, conn);
        }
    }

    public void atualizarStatus(Long pedidoId, br.com.agnellovinheria.model.enums.StatusPedido status, Long vinheriaId) throws Exception {
        try (Connection conn = getConnection()) {
            Optional<Pedido> op = pedidoDAO.findById(pedidoId, conn);
            if (op.isEmpty() || !Objects.equals(op.get().getVinheriaId(), vinheriaId)) {
                throw new Exception("Pedido não encontrado ou não autorizado.");
            }
            int affected = pedidoDAO.updateStatus(pedidoId, status, conn);
            if (affected == 0) {
                throw new Exception("Nenhum pedido foi atualizado.");
            }
        }
    }

    /* ─── Dashboard-specific methods ──────────────────────────── */

    /**
     * Returns the N most recent orders for the dashboard summary table.
     */
    public List<PedidoResumo> listarRecentes(Long vinheriaId, int limit) {
        try (Connection conn = getConnection()) {
            return pedidoDAO.findRecentByVinheriaId(vinheriaId, limit, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar pedidos recentes", e);
            return Collections.emptyList();
        }
    }

    /**
     * Returns the total sales amount (sum of {@code total}) for the current month,
     * excluding cancelled orders.
     */
    public BigDecimal calcularVendasMes(Long vinheriaId) {
        LocalDate today = LocalDate.now();
        return calcularVendasMes(vinheriaId, today.getYear(), today.getMonthValue());
    }

    /**
     * Returns the total sales amount for the previous month (for variation calculation).
     */
    public BigDecimal calcularVendasMesAnterior(Long vinheriaId) {
        LocalDate prev = LocalDate.now().minusMonths(1);
        return calcularVendasMes(vinheriaId, prev.getYear(), prev.getMonthValue());
    }

    /**
     * Internal helper — sums order totals for a specific year/month.
     */
    BigDecimal calcularVendasMes(Long vinheriaId, int year, int month) {
        try (Connection conn = getConnection()) {
            return pedidoDAO.sumTotalByVinheriaAndMonth(vinheriaId, year, month, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao calcular vendas do mês", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Counts orders that are still pending (not delivered or cancelled).
     */
    public int contarPedidosPendentes(Long vinheriaId) {
        try (Connection conn = getConnection()) {
            return pedidoDAO.countPending(vinheriaId, conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao contar pedidos pendentes", e);
            return 0;
        }
    }
}
