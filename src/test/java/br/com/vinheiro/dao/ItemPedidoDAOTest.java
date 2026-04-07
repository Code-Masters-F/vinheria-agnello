package br.com.vinheiro.dao;

import br.com.vinheiro.model.ItemPedido;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

public class ItemPedidoDAOTest extends BaseDAOTest {

    @Test
    public void testSaveAndFindById() throws Exception {
        ItemPedidoDAO dao = new ItemPedidoDAO();
        ItemPedido item = new ItemPedido();
        
        long testPedidoId = 999L;
        long testVinhoId = 888L;
        
        item.setPedidoId(testPedidoId);
        item.setVinhoId(testVinhoId);
        item.setQuantidade(2);
        item.setPrecoUnit(new BigDecimal("150.50"));

        dao.save(item, connection);

        Assertions.assertTrue(item.getId() > 0);

        Optional<ItemPedido> found = dao.findById(item.getId(), connection);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(2, found.get().getQuantidade());
        Assertions.assertEquals(new BigDecimal("150.50"), found.get().getPrecoUnit());
    }

    @Test
    public void testFindByPedidoId() throws Exception {
        ItemPedidoDAO dao = new ItemPedidoDAO();
        long testPedidoId = 123L;
        
        ItemPedido item1 = new ItemPedido(null, testPedidoId, 1L, 1, new BigDecimal("10.0"));
        ItemPedido item2 = new ItemPedido(null, testPedidoId, 2L, 2, new BigDecimal("20.0"));
        dao.save(item1, connection);
        dao.save(item2, connection);
        
        List<ItemPedido> itens = dao.findByPedidoId(testPedidoId, connection);
        Assertions.assertEquals(2, itens.size());
    }
}
