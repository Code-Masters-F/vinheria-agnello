package br.com.agnellovinheria.dao;

import br.com.agnellovinheria.model.Pedido;
import br.com.agnellovinheria.model.PedidoResumo;
import br.com.agnellovinheria.model.enums.StatusPedido;
import br.com.agnellovinheria.model.enums.TipoEntrega;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class PedidoDAOTest extends BaseDAOTest {

    private PedidoDAO dao;
    private long testVinheriaId;

    @BeforeEach
    public void init() throws Exception {
        dao = new PedidoDAO();

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO vinheria (nome, slug) VALUES ('Test Vinheria Pedido', 'test-vinheria-pedido')", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    testVinheriaId = rs.getLong(1);
                }
            }
        }
    }

    @Test
    public void testSaveAndFindById() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setVinheriaId(testVinheriaId);
        pedido.setStatus(StatusPedido.aguardando_pagamento);
        pedido.setTipoEntrega(TipoEntrega.delivery);
        pedido.setSubtotal(new BigDecimal("100.00"));
        pedido.setTotal(new BigDecimal("120.00"));
        pedido.setEnderecoEntrega("Avenue 123");

        dao.save(pedido, connection);

        Assertions.assertTrue(pedido.getId() > 0);

        Optional<Pedido> found = dao.findById(pedido.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(new BigDecimal("100.00"), found.get().getSubtotal());
        Assertions.assertEquals(StatusPedido.aguardando_pagamento, found.get().getStatus());
        Assertions.assertEquals("Avenue 123", found.get().getEnderecoEntrega());
    }

    /* ─── New tests for dashboard queries ─────────────────────── */

    @Test
    @DisplayName("findRecentByVinheriaId: should return limited results ordered by date DESC")
    public void testFindRecentByVinheriaId() throws Exception {
        // Setup: insert a cliente and a vinho for the JOIN
        long clienteId;
        long vinhoId;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("INSERT INTO cliente (vinheria_id, nome, email, tipo_cadastro) " +
                    "VALUES (" + testVinheriaId + ", 'Maria Silva', 'maria@test.com', 'completo')",
                    Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                clienteId = rs.getLong(1);
            }

            stmt.executeUpdate("INSERT INTO vinho (vinheria_id, nome, tipo, preco, estoque) " +
                    "VALUES (" + testVinheriaId + ", 'Malbec Reserve', 'tinto', 89.90, 10)",
                    Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) {
                rs.next();
                vinhoId = rs.getLong(1);
            }
        }

        // Insert 3 orders
        for (int i = 1; i <= 3; i++) {
            Pedido p = new Pedido();
            p.setVinheriaId(testVinheriaId);
            p.setClienteId(clienteId);
            p.setStatus(StatusPedido.aguardando_pagamento);
            p.setTipoEntrega(TipoEntrega.delivery);
            p.setSubtotal(new BigDecimal(100 * i));
            p.setTotal(new BigDecimal(100 * i));
            p.setEnderecoEntrega("Rua " + i);
            dao.save(p, connection);

            // Link item_pedido
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("INSERT INTO item_pedido (pedido_id, vinho_id, quantidade, preco_unit) " +
                        "VALUES (" + p.getId() + ", " + vinhoId + ", 1, 89.90)");
            }
        }

        // Act: request only 2 recent
        List<PedidoResumo> result = dao.findRecentByVinheriaId(testVinheriaId, 2, connection);

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Maria Silva", result.get(0).getClienteNome());
        Assertions.assertEquals("Malbec Reserve", result.get(0).getVinhoNome());
        Assertions.assertNotNull(result.get(0).getId());
        Assertions.assertNotNull(result.get(0).getStatus());
    }

    @Test
    @DisplayName("findRecentByVinheriaId: should return empty list when no orders exist")
    public void testFindRecentByVinheriaId_empty() throws Exception {
        List<PedidoResumo> result = dao.findRecentByVinheriaId(testVinheriaId, 5, connection);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("sumTotalByVinheriaAndMonth: should sum totals excluding cancelled orders")
    public void testSumTotalByVinheriaAndMonth() throws Exception {
        // Insert orders for the current month
        java.time.LocalDate today = java.time.LocalDate.now();

        Pedido p1 = createOrder(testVinheriaId, StatusPedido.entregue, new BigDecimal("200.00"));
        Pedido p2 = createOrder(testVinheriaId, StatusPedido.pago, new BigDecimal("150.00"));
        Pedido p3 = createOrder(testVinheriaId, StatusPedido.cancelado, new BigDecimal("999.00")); // should be excluded

        BigDecimal sum = dao.sumTotalByVinheriaAndMonth(testVinheriaId, today.getYear(), today.getMonthValue(), connection);

        // Only p1 + p2 should be summed (350.00)
        Assertions.assertEquals(0, new BigDecimal("350.00").compareTo(sum),
                "Sum should be 350.00 (excluding cancelled), got: " + sum);
    }

    @Test
    @DisplayName("sumTotalByVinheriaAndMonth: should return zero when no orders exist")
    public void testSumTotalByVinheriaAndMonth_noOrders() throws Exception {
        BigDecimal sum = dao.sumTotalByVinheriaAndMonth(testVinheriaId, 2020, 1, connection);
        Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(sum));
    }

    @Test
    @DisplayName("countPending: should count orders not entregue/cancelado")
    public void testCountPending() throws Exception {
        createOrder(testVinheriaId, StatusPedido.aguardando_pagamento, new BigDecimal("100.00"));
        createOrder(testVinheriaId, StatusPedido.pago, new BigDecimal("100.00"));
        createOrder(testVinheriaId, StatusPedido.em_separacao, new BigDecimal("100.00"));
        createOrder(testVinheriaId, StatusPedido.entregue, new BigDecimal("100.00")); // excluded
        createOrder(testVinheriaId, StatusPedido.cancelado, new BigDecimal("100.00")); // excluded

        int pending = dao.countPending(testVinheriaId, connection);

        Assertions.assertEquals(3, pending, "Should count 3 pending orders");
    }

    @Test
    @DisplayName("countPending: should return zero when no pending orders")
    public void testCountPending_noPending() throws Exception {
        createOrder(testVinheriaId, StatusPedido.entregue, new BigDecimal("100.00"));
        createOrder(testVinheriaId, StatusPedido.cancelado, new BigDecimal("100.00"));

        int pending = dao.countPending(testVinheriaId, connection);

        Assertions.assertEquals(0, pending);
    }

    /* Helper to quickly create a test order */
    private Pedido createOrder(long vinheriaId, StatusPedido status, BigDecimal total) throws Exception {
        Pedido p = new Pedido();
        p.setVinheriaId(vinheriaId);
        p.setStatus(status);
        p.setTipoEntrega(TipoEntrega.delivery);
        p.setSubtotal(total);
        p.setTotal(total);
        p.setEnderecoEntrega("Test address");
        dao.save(p, connection);
        return p;
    }
}
