package br.com.vinheiro.dao;

import br.com.vinheiro.model.ItemPedido;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;

public class ItemPedidoDAOTest extends BaseDAOTest {
    private ItemPedidoDAO dao;
    private long testPedidoId;
    private long testVinhoId;

    @BeforeEach
    public void setup() throws Exception {
        dao = new ItemPedidoDAO();
        try (Statement stmt = connection.createStatement()) {
            // Create a test Vinheria first
            stmt.executeUpdate("INSERT INTO vinheria (nome, slug) VALUES ('Test Vinheria IP', 'test-ip')", Statement.RETURN_GENERATED_KEYS);
            long vinheriaId;
            try (var rs = stmt.getGeneratedKeys()) { rs.next(); vinheriaId = rs.getLong(1); }

            // Create a test Vinho
            stmt.executeUpdate("INSERT INTO vinho (vinheria_id, nome, tipo, preco) VALUES (" + vinheriaId + ", 'Test Vinho IP', 'tinto', 50.0)", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) { rs.next(); testVinhoId = rs.getLong(1); }

            // Create a test Pedido
            stmt.executeUpdate("INSERT INTO pedido (vinheria_id, tipo_entrega, subtotal, total) VALUES (" + vinheriaId + ", 'entrega', 100.0, 100.0)", Statement.RETURN_GENERATED_KEYS);
            try (var rs = stmt.getGeneratedKeys()) { rs.next(); testPedidoId = rs.getLong(1); }
        }
    }

    @Test
    public void testSaveAndFindById() throws Exception {
        ItemPedido item = new ItemPedido();
        item.setPedidoId(testPedidoId);
        item.setVinhoId(testVinhoId);
        item.setQuantidade(2);
        item.setPrecoUnit(new BigDecimal("150.50"));

        dao.save(item, connection);

        Assertions.assertNotNull(item.getId());
        Optional<ItemPedido> found = dao.findById(item.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(2, found.get().getQuantidade());
    }

    @Test
    public void testFindByPedidoId() throws Exception {
        ItemPedido item1 = new ItemPedido(null, testPedidoId, testVinhoId, 1, new BigDecimal("10.0"));
        ItemPedido item2 = new ItemPedido(null, testPedidoId, testVinhoId, 2, new BigDecimal("20.0"));
        dao.save(item1, connection);
        dao.save(item2, connection);
        
        List<ItemPedido> itens = dao.findByPedidoId(testPedidoId, connection);
        Assertions.assertTrue(itens.size() >= 2);
    }
}
