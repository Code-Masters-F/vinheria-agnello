package br.com.agnellovinheria.servlet.admin;

import br.com.agnellovinheria.model.PedidoResumo;
import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinho;
import br.com.agnellovinheria.service.PedidoService;
import br.com.agnellovinheria.service.VinhoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * DashboardServlet — Controller for the Admin Dashboard page.
 *
 * Responsible for aggregating business metrics (wine count, low-stock alerts,
 * monthly sales, pending orders, recent orders) and forwarding them to
 * dashboard.jsp for rendering.
 *
 * Security: Protected by AuthFilter + TenantFilter on /admin/* path.
 * Architecture: Delegates all data access through the Service layer.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/admin/dashboard", "/admin/"})
public class DashboardServlet extends HttpServlet {

    /** Maximum number of recent orders shown on the dashboard. */
    private static final int RECENT_ORDERS_LIMIT = 5;

    private VinhoService vinhoService;
    private PedidoService pedidoService;

    @Override
    public void init() throws ServletException {
        this.vinhoService = new VinhoService();
        this.pedidoService = new PedidoService();
    }

    /* Package-private constructor for testability (Mockito injection). */
    DashboardServlet(VinhoService vinhoService, PedidoService pedidoService) {
        this.vinhoService = vinhoService;
        this.pedidoService = pedidoService;
    }

    public DashboardServlet() {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Guard: verify session (AuthFilter normally handles this, but we double-check)
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        UsuarioAdmin admin = (UsuarioAdmin) session.getAttribute("usuarioAdmin");
        if (admin == null || admin.getVinheria() == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        Long vinheriaId = admin.getVinheria().getId();

        // ── Wine metrics ────────────────────────────────────────
        List<Vinho> vinhos = vinhoService.listarDisponiveis(vinheriaId);

        int qtdAlertasEstoque = (int) vinhos.stream()
                .filter(v -> v.getEstoque() <= v.getEstoqueMinimo())
                .count();

        // Severity classification for the stock alert KPI badge
        String severidadeEstoque;
        boolean temEstoqueZero = vinhos.stream()
                .anyMatch(v -> v.getEstoque() <= v.getEstoqueMinimo() && v.getEstoque() == 0);
        if (qtdAlertasEstoque == 0) {
            severidadeEstoque = "Normal";
        } else if (temEstoqueZero) {
            severidadeEstoque = "Crítico";
        } else {
            severidadeEstoque = "Atenção";
        }

        // ── Order / sales metrics ───────────────────────────────
        BigDecimal vendasMes = pedidoService.calcularVendasMes(vinheriaId);
        BigDecimal vendasMesAnterior = pedidoService.calcularVendasMesAnterior(vinheriaId);
        double variacaoVendas = calcularVariacao(vendasMes, vendasMesAnterior);
        int pedidosPendentes = pedidoService.contarPedidosPendentes(vinheriaId);
        List<PedidoResumo> ultimosPedidos = pedidoService.listarRecentes(vinheriaId, RECENT_ORDERS_LIMIT);

        // ── Set page attributes for JSP ─────────────────────────
        req.setAttribute("currentPage",        "dashboard");
        req.setAttribute("pageTitle",          "Dashboard");
        req.setAttribute("pageSubtitle",       "Bem-vindo de volta, " + admin.getNome() + ".");

        // Wine KPIs
        req.setAttribute("totalVinhos",        vinhos.size());
        req.setAttribute("qtdAlertasEstoque",  qtdAlertasEstoque);
        req.setAttribute("severidadeEstoque",  severidadeEstoque);
        req.setAttribute("vinhos",             vinhos);

        // Sales & Order KPIs
        req.setAttribute("vendasMes",          vendasMes);
        req.setAttribute("variacaoVendas",     variacaoVendas);
        req.setAttribute("pedidosPendentes",   pedidosPendentes);
        req.setAttribute("ultimosPedidos",     ultimosPedidos);

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }

    /**
     * Calculates the percentage variation between current and previous values.
     * Returns 0.0 when the previous value is zero (avoids division by zero).
     */
    static double calcularVariacao(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return atual != null && atual.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
