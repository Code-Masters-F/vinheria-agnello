package br.com.agnellovinheria.servlet.admin;

import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.model.Vinho;
import br.com.agnellovinheria.service.VinhoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * DashboardServlet — Controller for the Admin Dashboard page.
 *
 * Responsible for aggregating business metrics (wine count, low-stock alerts,
 * recent orders) and forwarding them to dashboard.jsp for rendering.
 *
 * Security: Protected by AuthFilter + TenantFilter on /admin/* path.
 * Architecture: Delegates all data access through the Service layer.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/admin/dashboard", "/admin/"})
public class DashboardServlet extends HttpServlet {

    private VinhoService vinhoService;

    @Override
    public void init() throws ServletException {
        this.vinhoService = new VinhoService();
    }

    /* Package-private constructor for testability (Mockito injection). */
    DashboardServlet(VinhoService vinhoService) {
        this.vinhoService = vinhoService;
    }

    public DashboardServlet() {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Guard: verify session (AuthFilter normally handles this, but we double-check)
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }

        UsuarioAdmin admin = (UsuarioAdmin) session.getAttribute("usuarioAdmin");
        if (admin == null || admin.getVinheria() == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }
        Long vinheriaId = admin.getVinheria().getId();

        // Fetch metrics via service layer (never directly from DAO)
        List<Vinho> vinhos = vinhoService.listarDisponiveis(vinheriaId);

        // Count wines with stock below minimum (stock alert)
        int qtdAlertasEstoque = (int) vinhos.stream()
                .filter(v -> v.getEstoque() <= v.getEstoqueMinimo())
                .count();

        // Set page attributes for JSP
        req.setAttribute("currentPage",       "dashboard");
        req.setAttribute("pageTitle",         "Dashboard");
        req.setAttribute("pageSubtitle",      "Bem-vindo de volta. Aqui está o que aconteceu na adega.");
        req.setAttribute("totalVinhos",       vinhos.size());
        req.setAttribute("qtdAlertasEstoque", qtdAlertasEstoque);
        req.setAttribute("vinhos",            vinhos);

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
