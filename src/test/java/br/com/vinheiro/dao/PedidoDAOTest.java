package br.com.vinheiro.dao;

import br.com.vinheiro.model.Pedido;
import br.com.vinheiro.model.enums.StatusPedido;
import br.com.vinheiro.model.enums.TipoEntrega;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Statement;
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
}
