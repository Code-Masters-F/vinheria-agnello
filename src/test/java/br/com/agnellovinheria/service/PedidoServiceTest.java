package br.com.agnellovinheria.service;

import br.com.agnellovinheria.dao.ItemPedidoDAO;
import br.com.agnellovinheria.dao.PedidoDAO;
import br.com.agnellovinheria.dao.VinhoDAO;
import br.com.agnellovinheria.model.ItemPedido;
import br.com.agnellovinheria.model.Pedido;
import br.com.agnellovinheria.model.PedidoResumo;
import br.com.agnellovinheria.model.Vinho;
import br.com.agnellovinheria.model.enums.StatusPedido;
import br.com.agnellovinheria.service.exceptions.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

        when(vinhoDAO.findById(eq(1L), any(Connection.class))).thenReturn(Optional.of(vinhoDb));

        assertThrows(InsufficientStockException.class, () -> {
            pedidoService.criarPedido(pedido, itens);
        });

        verify(connection).rollback();
    }

    /* ─── Dashboard service method tests ──────────────────────── */

    @Test
    @DisplayName("listarRecentes: should delegate to DAO and return results")
    void listarRecentes_shouldReturnResults() throws Exception {
        PedidoResumo r1 = new PedidoResumo(1L, "Cliente A", "Malbec", LocalDateTime.now(),
                new BigDecimal("120.00"), StatusPedido.entregue);
        PedidoResumo r2 = new PedidoResumo(2L, "Cliente B", "Cabernet", LocalDateTime.now(),
                new BigDecimal("89.00"), StatusPedido.pago);

        when(pedidoDAO.findRecentByVinheriaId(eq(10L), eq(5), any(Connection.class)))
                .thenReturn(Arrays.asList(r1, r2));

        List<PedidoResumo> result = pedidoService.listarRecentes(10L, 5);

        assertEquals(2, result.size());
        assertEquals("Cliente A", result.get(0).getClienteNome());
        verify(pedidoDAO).findRecentByVinheriaId(10L, 5, connection);
    }

    @Test
    @DisplayName("listarRecentes: should return empty list on SQLException")
    void listarRecentes_shouldReturnEmptyOnError() throws Exception {
        when(pedidoDAO.findRecentByVinheriaId(anyLong(), anyInt(), any(Connection.class)))
                .thenThrow(new SQLException("DB down"));

        List<PedidoResumo> result = pedidoService.listarRecentes(10L, 5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("calcularVendasMes: should return sum for current month")
    void calcularVendasMes_shouldReturnSum() throws Exception {
        LocalDate today = LocalDate.now();
        when(pedidoDAO.sumTotalByVinheriaAndMonth(eq(10L), eq(today.getYear()),
                eq(today.getMonthValue()), any(Connection.class)))
                .thenReturn(new BigDecimal("5000.50"));

        BigDecimal result = pedidoService.calcularVendasMes(10L);

        assertEquals(new BigDecimal("5000.50"), result);
    }

    @Test
    @DisplayName("calcularVendasMesAnterior: should query previous month")
    void calcularVendasMesAnterior_shouldQueryPreviousMonth() throws Exception {
        LocalDate prev = LocalDate.now().minusMonths(1);
        when(pedidoDAO.sumTotalByVinheriaAndMonth(eq(10L), eq(prev.getYear()),
                eq(prev.getMonthValue()), any(Connection.class)))
                .thenReturn(new BigDecimal("3200.00"));

        BigDecimal result = pedidoService.calcularVendasMesAnterior(10L);

        assertEquals(new BigDecimal("3200.00"), result);
    }

    @Test
    @DisplayName("calcularVendasMes: should return zero on SQLException")
    void calcularVendasMes_shouldReturnZeroOnError() throws Exception {
        when(pedidoDAO.sumTotalByVinheriaAndMonth(anyLong(), anyInt(), anyInt(), any(Connection.class)))
                .thenThrow(new SQLException("DB down"));

        BigDecimal result = pedidoService.calcularVendasMes(10L);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("contarPedidosPendentes: should delegate to DAO")
    void contarPedidosPendentes_shouldReturnCount() throws Exception {
        when(pedidoDAO.countPending(eq(10L), any(Connection.class))).thenReturn(7);

        int result = pedidoService.contarPedidosPendentes(10L);

        assertEquals(7, result);
        verify(pedidoDAO).countPending(10L, connection);
    }

    @Test
    @DisplayName("contarPedidosPendentes: should return zero on SQLException")
    void contarPedidosPendentes_shouldReturnZeroOnError() throws Exception {
        when(pedidoDAO.countPending(anyLong(), any(Connection.class)))
                .thenThrow(new SQLException("DB down"));

        int result = pedidoService.contarPedidosPendentes(10L);

        assertEquals(0, result);
    }
}
