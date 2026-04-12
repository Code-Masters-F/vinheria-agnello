package br.com.agnellovinheria.servlet.admin;

import br.com.agnellovinheria.model.Pedido;
import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;
import br.com.agnellovinheria.model.enums.StatusPedido;
import br.com.agnellovinheria.service.PedidoService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PedidosServlet (TDD — tests before implementation).
 */
@ExtendWith(MockitoExtension.class)
class PedidosServletTest {

    @Mock private PedidoService pedidoService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @InjectMocks
    private PedidosServlet servlet;

    private UsuarioAdmin admin;

    @BeforeEach
    void setUp() {
        Vinheria vinheria = new Vinheria();
        vinheria.setId(1L);

        admin = new UsuarioAdmin();
        admin.setId(1L);
        admin.setNome("Admin Teste");
        admin.setVinheria(vinheria);
    }

    @Test
    @DisplayName("Pedidos: GET should forward to pedidos.jsp with orders list")
    void doGet_shouldForwardWithOrders() throws Exception {
        Pedido p = new Pedido(1L, 1L, 10L, BigDecimal.valueOf(150));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(request.getRequestDispatcher("/WEB-INF/views/admin/pedidos.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("currentPage"), eq("pedidos"));
        verify(request).setAttribute(eq("statusOptions"), any(StatusPedido[].class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Pedidos: POST status update without pedidoId should forward with error")
    void doPost_missingPedidoId_shouldForwardWithError() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(request.getParameter("pedidoId")).thenReturn(null);
        when(request.getParameter("novoStatus")).thenReturn("pago");
        when(request.getRequestDispatcher("/WEB-INF/views/admin/pedidos.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
    }
}
