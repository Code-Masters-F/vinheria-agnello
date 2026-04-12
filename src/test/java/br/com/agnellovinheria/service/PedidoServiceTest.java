package br.com.agnellovinheria.service;

import br.com.agnellovinheria.dao.ItemPedidoDAO;
import br.com.agnellovinheria.dao.PedidoDAO;
import br.com.agnellovinheria.dao.VinhoDAO;
import br.com.agnellovinheria.model.ItemPedido;
import br.com.agnellovinheria.model.Pedido;
import br.com.agnellovinheria.model.Vinho;
import br.com.agnellovinheria.service.exceptions.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoDAO pedidoDAO;
    
    @Mock
    private ItemPedidoDAO itemPedidoDAO;
    
    @Mock
    private VinhoDAO vinhoDAO;

    @Mock
    private Connection connection;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(pedidoDAO, itemPedidoDAO, vinhoDAO) {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }

    @Test
    void criarPedido_WithInsufficientStock_ShouldThrowException() throws Exception {
        Pedido pedido = new Pedido();
        List<ItemPedido> itens = new ArrayList<>();
        ItemPedido item = new ItemPedido();
        item.setVinhoId(1L);
        item.setQuantidade(5);
        itens.add(item);

        Vinho vinhoDb = new Vinho();
        vinhoDb.setId(1L);
        vinhoDb.setEstoque(2); // Less than 5!

        // Pessimistic lock expected -> some findByIdForUpdate method or standard findById 
        when(vinhoDAO.findById(eq(1L), any(Connection.class))).thenReturn(Optional.of(vinhoDb));

        assertThrows(InsufficientStockException.class, () -> {
            pedidoService.criarPedido(pedido, itens);
        });

        verify(connection).rollback();
    }
}
