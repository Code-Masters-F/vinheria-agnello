package br.com.agnellovinheria.servlet.admin;

import br.com.agnellovinheria.model.PedidoResumo;
import br.com.agnellovinheria.model.Vinho;
import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinheria;
import br.com.agnellovinheria.model.enums.StatusPedido;
import br.com.agnellovinheria.service.PedidoService;
import br.com.agnellovinheria.service.VinhoService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DashboardServlet using Mockito.
 * Updated for the dashboard redesign (Spec 005).
 */
@ExtendWith(MockitoExtension.class)
class DashboardServletTest {

    @Mock private VinhoService vinhoService;
    @Mock private PedidoService pedidoService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

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

        servlet = new DashboardServlet(vinhoService, pedidoService);
    }

    @Test
    @DisplayName("Dashboard: GET should set all metric attributes and forward to dashboard.jsp")
    void doGet_shouldSetMetricsAndForward() throws Exception {
        // Arrange
        List<Vinho> vinhos = Arrays.asList(new Vinho(), new Vinho());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(vinhos);
        when(pedidoService.calcularVendasMes(1L)).thenReturn(new BigDecimal("5000.00"));
        when(pedidoService.calcularVendasMesAnterior(1L)).thenReturn(new BigDecimal("4000.00"));
        when(pedidoService.contarPedidosPendentes(1L)).thenReturn(3);
        when(pedidoService.listarRecentes(1L, 5)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        // Act
        servlet.doGet(request, response);

        // Assert — page attributes
        verify(request).setAttribute(eq("currentPage"), eq("dashboard"));
        verify(request).setAttribute(eq("pageTitle"), eq("Dashboard"));
        verify(request).setAttribute(eq("totalVinhos"), eq(2));

        // Assert — sales & order KPIs
        verify(request).setAttribute(eq("vendasMes"), eq(new BigDecimal("5000.00")));
        verify(request).setAttribute(eq("pedidosPendentes"), eq(3));
        verify(request).setAttribute(eq("ultimosPedidos"), any(List.class));

        // Assert — variation should be +25.0% (5000-4000)/4000*100
        verify(request).setAttribute(eq("variacaoVendas"), eq(25.0));

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
    @DisplayName("Dashboard: GET with low stock wines should include stockAlerts and severity")
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
        when(pedidoService.calcularVendasMes(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.calcularVendasMesAnterior(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.contarPedidosPendentes(1L)).thenReturn(0);
        when(pedidoService.listarRecentes(1L, 5)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        // Act
        servlet.doGet(request, response);

        // Assert — only 1 wine is below stock minimum
        verify(request).setAttribute(eq("qtdAlertasEstoque"), eq(1));
        // Severity should be "Atenção" (low stock > 0, not zero stock)
        verify(request).setAttribute(eq("severidadeEstoque"), eq("Atenção"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Dashboard: severity should be 'Crítico' when a wine has zero stock")
    void doGet_withZeroStock_shouldSetCriticalSeverity() throws Exception {
        Vinho estoqueZero = new Vinho();
        estoqueZero.setEstoque(0);
        estoqueZero.setEstoqueMinimo(5);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(List.of(estoqueZero));
        when(pedidoService.calcularVendasMes(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.calcularVendasMesAnterior(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.contarPedidosPendentes(1L)).thenReturn(0);
        when(pedidoService.listarRecentes(1L, 5)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("severidadeEstoque"), eq("Crítico"));
    }

    @Test
    @DisplayName("Dashboard: severity should be 'Normal' when no stock alerts")
    void doGet_withNoAlerts_shouldSetNormalSeverity() throws Exception {
        Vinho estoqueOk = new Vinho();
        estoqueOk.setEstoque(20);
        estoqueOk.setEstoqueMinimo(5);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(List.of(estoqueOk));
        when(pedidoService.calcularVendasMes(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.calcularVendasMesAnterior(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.contarPedidosPendentes(1L)).thenReturn(0);
        when(pedidoService.listarRecentes(1L, 5)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("qtdAlertasEstoque"), eq(0));
        verify(request).setAttribute(eq("severidadeEstoque"), eq("Normal"));
    }

    @Test
    @DisplayName("Dashboard: variation should be 0% when both months have zero sales")
    void doGet_withZeroSales_shouldSetZeroVariation() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(Collections.emptyList());
        when(pedidoService.calcularVendasMes(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.calcularVendasMesAnterior(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.contarPedidosPendentes(1L)).thenReturn(0);
        when(pedidoService.listarRecentes(1L, 5)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("vendasMes"), eq(BigDecimal.ZERO));
        verify(request).setAttribute(eq("variacaoVendas"), eq(0.0));
    }

    @Test
    @DisplayName("Dashboard: variation should be 100% when previous month is zero but current has sales")
    void doGet_withSalesFromZero_shouldSet100Variation() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioAdmin")).thenReturn(admin);
        when(vinhoService.listarDisponiveis(1L)).thenReturn(Collections.emptyList());
        when(pedidoService.calcularVendasMes(1L)).thenReturn(new BigDecimal("1000.00"));
        when(pedidoService.calcularVendasMesAnterior(1L)).thenReturn(BigDecimal.ZERO);
        when(pedidoService.contarPedidosPendentes(1L)).thenReturn(0);
        when(pedidoService.listarRecentes(1L, 5)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("variacaoVendas"), eq(100.0));
    }

    /* ─── Unit test for the static helper ─────────────────────── */

    @Test
    @DisplayName("calcularVariacao: basic percentage calculation")
    void calcularVariacao_basic() {
        assertEquals(25.0, DashboardServlet.calcularVariacao(
                new BigDecimal("5000"), new BigDecimal("4000")));
    }

    @Test
    @DisplayName("calcularVariacao: negative variation")
    void calcularVariacao_negative() {
        assertEquals(-20.0, DashboardServlet.calcularVariacao(
                new BigDecimal("4000"), new BigDecimal("5000")));
    }

    @Test
    @DisplayName("calcularVariacao: zero previous with non-zero current returns 100%")
    void calcularVariacao_zeroPrevious() {
        assertEquals(100.0, DashboardServlet.calcularVariacao(
                new BigDecimal("1000"), BigDecimal.ZERO));
    }

    @Test
    @DisplayName("calcularVariacao: both zero returns 0%")
    void calcularVariacao_bothZero() {
        assertEquals(0.0, DashboardServlet.calcularVariacao(BigDecimal.ZERO, BigDecimal.ZERO));
    }
}
