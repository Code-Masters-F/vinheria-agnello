package br.com.vinheiro.servlet.admin;

import br.com.vinheiro.model.Vinho;
import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.model.UsuarioAdmin;
import br.com.vinheiro.model.enums.TipoVinho;
import br.com.vinheiro.service.VinhoService;
import br.com.vinheiro.service.exceptions.InvalidDataException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CatalogoServlet — Controller for the Admin Wine Catalog page.
 *
 * Handles:
 *   GET  /admin/catalogo          — lists wines for the current tenant
 *   POST /admin/catalogo (create) — creates a new wine
 *   POST /admin/catalogo (update) — updates an existing wine
 *   POST /admin/catalogo (delete) — deactivates a wine
 *
 * Uses PRG (Post-Redirect-Get) pattern to prevent duplicate submissions.
 * All DB access delegated to VinhoService.
 */
@WebServlet(name = "CatalogoServlet", urlPatterns = "/admin/catalogo")
public class CatalogoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CatalogoServlet.class.getName());

    private VinhoService vinhoService;

    @Override
    public void init() throws ServletException {
        this.vinhoService = new VinhoService();
    }

    /** Package-private constructor for Mockito injection in tests. */
    CatalogoServlet(VinhoService vinhoService) {
        this.vinhoService = vinhoService;
    }

    public CatalogoServlet() {}

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

        // Check for flash messages from previous POST (PRG pattern)
        String success = (String) session.getAttribute("successMessage");
        String error   = (String) session.getAttribute("errorMessage");
        if (success != null) { req.setAttribute("successMessage", success); session.removeAttribute("successMessage"); }
        if (error   != null) { req.setAttribute("errorMessage",   error);   session.removeAttribute("errorMessage"); }

        List<Vinho> vinhos = vinhoService.listarDisponiveis(vinheriaId);

        req.setAttribute("currentPage",  "catalogo");
        req.setAttribute("pageTitle",    "Catálogo de Vinhos");
        req.setAttribute("pageSubtitle", "Gerencie o portfólio da sua adega.");
        req.setAttribute("vinhos",       vinhos);
        req.setAttribute("tipos",        TipoVinho.values());

        req.getRequestDispatcher("/WEB-INF/views/admin/catalogo.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }

        UsuarioAdmin admin = (UsuarioAdmin) session.getAttribute("usuarioAdmin");
        Long vinheriaId = admin.getVinheria().getId();

        String action = req.getParameter("action");

        if ("create".equals(action)) {
            handleCreate(req, resp, session, vinheriaId);
        } else if ("update".equals(action)) {
            handleUpdate(req, resp, session, vinheriaId);
        } else if ("delete".equals(action)) {
            handleDelete(req, resp, session, vinheriaId);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/catalogo");
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp,
                               HttpSession session, Long vinheriaId)
            throws ServletException, IOException {

        String nome   = req.getParameter("nome");
        String preco  = req.getParameter("preco");
        String tipo   = req.getParameter("tipo");
        String estStr = req.getParameter("estoque");

        // Inline validation
        if (nome == null || nome.isBlank()) {
            forwardWithError(req, resp, vinheriaId, "O nome do vinho é obrigatório.");
            return;
        }

        Vinho vinho = buildVinhoFromParams(req, vinheriaId);

        try {
            vinhoService.cadastrarVinho(vinho, vinheriaId);
            session.setAttribute("successMessage", "Vinho \"" + nome + "\" cadastrado com sucesso!");
            resp.sendRedirect(req.getContextPath() + "/admin/catalogo");
        } catch (InvalidDataException e) {
            forwardWithError(req, resp, vinheriaId, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating wine", e);
            forwardWithError(req, resp, vinheriaId, "Erro interno ao cadastrar o vinho. Tente novamente.");
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp,
                               HttpSession session, Long vinheriaId)
            throws ServletException, IOException {

        String nome = req.getParameter("nome");
        if (nome == null || nome.isBlank()) {
            forwardWithError(req, resp, vinheriaId, "O nome do vinho é obrigatório.");
            return;
        }

        Vinho vinho = buildVinhoFromParams(req, vinheriaId);
        try {
            vinhoService.cadastrarVinho(vinho, vinheriaId); // Service handles upsert
            session.setAttribute("successMessage", "Vinho atualizado com sucesso!");
            resp.sendRedirect(req.getContextPath() + "/admin/catalogo");
        } catch (InvalidDataException e) {
            forwardWithError(req, resp, vinheriaId, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating wine", e);
            forwardWithError(req, resp, vinheriaId, "Erro interno ao atualizar o vinho.");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp,
                               HttpSession session, Long vinheriaId)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            session.setAttribute("errorMessage", "ID do vinho não informado.");
            resp.sendRedirect(req.getContextPath() + "/admin/catalogo");
            return;
        }

        try {
            long vinhoId = Long.parseLong(idParam);
            // Deactivation (soft delete) via stock update to 0
            vinhoService.atualizarEstoque(vinhoId, 0);
            session.setAttribute("successMessage", "Vinho desativado com sucesso.");
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID do vinho inválido.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deactivating wine", e);
            session.setAttribute("errorMessage", "Erro ao desativar o vinho.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/catalogo");
    }

    /** Builds a Vinho instance from form parameters. */
    private Vinho buildVinhoFromParams(HttpServletRequest req, Long vinheriaId) {
        Vinho vinho = new Vinho();

        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isBlank()) {
            vinho.setId(Long.parseLong(idParam));
        }

        vinho.setNome(req.getParameter("nome"));
        vinho.setPreco(req.getParameter("preco"));
        vinho.setDescricao(req.getParameter("descricao"));
        vinho.setUva(req.getParameter("uva"));
        vinho.setPais(req.getParameter("pais"));
        vinho.setRegiao(req.getParameter("regiao"));
        vinho.setSafra(req.getParameter("safra"));
        vinho.setFotoUrl(req.getParameter("fotoUrl"));
        vinho.setAtivo(true);

        String tipoParam = req.getParameter("tipo");
        if (tipoParam != null && !tipoParam.isBlank()) {
            try { vinho.setTipo(TipoVinho.valueOf(tipoParam)); } catch (IllegalArgumentException ignored) {}
        }

        String estStr = req.getParameter("estoque");
        if (estStr != null && !estStr.isBlank()) {
            try { vinho.setEstoque(Integer.parseInt(estStr)); } catch (NumberFormatException ignored) {}
        }

        String estMinStr = req.getParameter("estoqueMinimo");
        if (estMinStr != null && !estMinStr.isBlank()) {
            try { vinho.setEstoqueMinimo(Integer.parseInt(estMinStr)); } catch (NumberFormatException ignored) {}
        }

        Vinheria v = new Vinheria();
        v.setId(vinheriaId);
        vinho.setVinheria(v);

        return vinho;
    }

    /** Helper to set error and re-render the catalog form (no redirect). */
    private void forwardWithError(HttpServletRequest req, HttpServletResponse resp,
                                   Long vinheriaId, String message)
            throws ServletException, IOException {

        req.setAttribute("errorMessage",  message);
        req.setAttribute("currentPage",   "catalogo");
        req.setAttribute("pageTitle",     "Catálogo de Vinhos");
        req.setAttribute("vinhos",        vinhoService.listarDisponiveis(vinheriaId));
        req.setAttribute("tipos",         TipoVinho.values());
        req.getRequestDispatcher("/WEB-INF/views/admin/catalogo.jsp").forward(req, resp);
    }
}
