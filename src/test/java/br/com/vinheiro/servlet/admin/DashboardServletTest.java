package br.com.vinheiro.servlet.admin;

import br.com.vinheiro.model.Pedido;
import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.model.UsuarioAdmin;
import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.service.PedidoService;
import br.com.vinheiro.service.VinhoService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardServlet using Mockito.
 * Tests are written before the implementation (TDD).
 */
@ExtendWith(MockitoExtension.class)
class DashboardServletTest {

    @Mock private VinhoService vinhoService;
    @Mock private PedidoService pedidoService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @InjectMocks
    private DashboardServlet servlet;

    private UsuarioAdmin admin;
    private Vinheria vinheria;

    @BeforeEach
    void setUp() {
        vinheria = new Vinheria();
        vinheria.setId(1L);
        vinheria.setNome("Vinheria Demo");

        admin = new UsuarioAdmin();
        admin.setId(1L);
        admin.setNome("Admin Teste");
        admin.setVinheria(vinheria);
    }

    @Test
    @DisplayName("Dashboard: GET should set metric attributes and forward to dashboard.jsp")
    void doGet_shouldSetMetricsAndForward() throws Exception {
        // Arrange
        List<Vinho> vinhos = Arrays.asList(new Vinho(), new Vinho());
        List<Pedido> pedidosPendentes = Collections.singletonList(new Pedido());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(vinhos);
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        // Act
        servlet.doGet(request, response);

        // Assert — verifica que os atributos certos foram setados
        verify(request).setAttribute(eq("currentPage"), eq("dashboard"));
        verify(request).setAttribute(eq("pageTitle"), eq("Dashboard"));
        verify(request).setAttribute(eq("totalVinhos"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Dashboard: GET without session should redirect to login")
    void doGet_withoutSession_shouldRedirectToLogin() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).sendRedirect(anyString());
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    @DisplayName("Dashboard: GET with low stock wines should include stockAlerts attribute")
    void doGet_withLowStock_shouldSetStockAlerts() throws Exception {
        // Arrange
        Vinho baixoEstoque = new Vinho();
        baixoEstoque.setEstoque(1);
        baixoEstoque.setEstoqueMinimo(5);

        Vinho estoqueOk = new Vinho();
        estoqueOk.setEstoque(10);
        estoqueOk.setEstoqueMinimo(5);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(Arrays.asList(baixoEstoque, estoqueOk));
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        // Act
        servlet.doGet(request, response);

        // Assert — apenas 1 vinho está abaixo do estoque mínimo
        verify(request).setAttribute(eq("qtdAlertasEstoque"), eq(1));
        verify(dispatcher).forward(request, response);
    }
}
