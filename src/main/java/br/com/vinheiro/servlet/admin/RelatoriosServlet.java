package br.com.vinheiro.servlet.admin;

import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.model.UsuarioAdmin;
import br.com.vinheiro.service.VinhoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RelatoriosServlet — Controller for the Admin Analytics & Reports page.
 *
 * GET /admin/relatorios → aggregates wine data and renders the reports view.
 *
 * Data Source: VinhoService (no direct DAO access from the controller).
 */
@WebServlet(name = "RelatoriosServlet", urlPatterns = "/admin/relatorios")
public class RelatoriosServlet extends HttpServlet {

    private VinhoService vinhoService;

    @Override
    public void init() throws ServletException {
        this.vinhoService = new VinhoService();
    }

    /** Package-private constructor for Mockito injection. */
    RelatoriosServlet(VinhoService vinhoService) {
        this.vinhoService = vinhoService;
    }

    public RelatoriosServlet() {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }

        UsuarioAdmin admin = (UsuarioAdmin) session.getAttribute("usuarioAdmin");
        Long vinheriaId = admin.getVinheria().getId();

        List<Vinho> vinhos = vinhoService.listarDisponiveis(vinheriaId);

        // Aggregate: count wines per type
        Map<String, Long> vinhosPorTipo = vinhos.stream()
                .filter(v -> v.getTipo() != null)
                .collect(Collectors.groupingBy(v -> v.getTipo().name(), Collectors.counting()));

        // Stock alerts: wines at or below minimum stock
        List<Vinho> alertasEstoque = vinhos.stream()
                .filter(v -> v.getEstoque() <= v.getEstoqueMinimo())
                .collect(Collectors.toList());

        // Top wines by stock (proxy for availability)
        List<Vinho> topVinhos = vinhos.stream()
                .sorted(Comparator.comparingInt(Vinho::getEstoque).reversed())
                .limit(5)
                .collect(Collectors.toList());

        req.setAttribute("currentPage",    "relatorios");
        req.setAttribute("pageTitle",      "Relatórios");
        req.setAttribute("pageSubtitle",   "Análise de desempenho e inventário da adega.");
        req.setAttribute("vinhos",         vinhos);
        req.setAttribute("vinhosPorTipo",  vinhosPorTipo);
        req.setAttribute("alertasEstoque", alertasEstoque);
        req.setAttribute("topVinhos",      topVinhos);
        req.setAttribute("totalVinhos",    vinhos.size());

        req.getRequestDispatcher("/WEB-INF/views/admin/relatorios.jsp").forward(req, resp);
    }
}
