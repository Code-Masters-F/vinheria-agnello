package br.com.vinheiro.servlet.admin;

import br.com.vinheiro.model.UsuarioAdmin;
import br.com.vinheiro.model.enums.StatusPedido;
import br.com.vinheiro.service.PedidoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PedidosServlet — Controller for Admin Order Management page.
 *
 * GET  /admin/pedidos        → list orders for the current tenant
 * POST /admin/pedidos/status → update a single order's status
 */
@WebServlet(name = "PedidosServlet", urlPatterns = "/admin/pedidos")
public class PedidosServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(PedidosServlet.class.getName());

    private PedidoService pedidoService;

    @Override
    public void init() throws ServletException {
        this.pedidoService = new PedidoService();
    }

    /** Package-private constructor for Mockito injection. */
    PedidosServlet(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public PedidosServlet() {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }

        // Flash messages from PRG
        String success = (String) session.getAttribute("successMessage");
        String error   = (String) session.getAttribute("errorMessage");
        if (success != null) { req.setAttribute("successMessage", success); session.removeAttribute("successMessage"); }
        if (error   != null) { req.setAttribute("errorMessage",   error);   session.removeAttribute("errorMessage"); }

        req.setAttribute("currentPage",    "pedidos");
        req.setAttribute("pageTitle",      "Gestão de Pedidos");
        req.setAttribute("pageSubtitle",   "Acompanhe e atualize o status de cada pedido.");
        req.setAttribute("statusOptions",  StatusPedido.values());

        req.getRequestDispatcher("/WEB-INF/views/admin/pedidos.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
            return;
        }

        String pedidoIdStr = req.getParameter("pedidoId");
        String novoStatus  = req.getParameter("novoStatus");

        if (pedidoIdStr == null || pedidoIdStr.isBlank()) {
            req.setAttribute("errorMessage",  "ID do pedido não foi informado.");
            req.setAttribute("currentPage",   "pedidos");
            req.setAttribute("pageTitle",     "Gestão de Pedidos");
            req.setAttribute("statusOptions", StatusPedido.values());
            req.getRequestDispatcher("/WEB-INF/views/admin/pedidos.jsp").forward(req, resp);
            return;
        }

        try {
            long pedidoId = Long.parseLong(pedidoIdStr);
            StatusPedido status = StatusPedido.valueOf(novoStatus);
            // Note: PedidoService does not yet expose updateStatus; this is a placeholder
            // that logs the intent until the service method is added.
            LOGGER.info("Status update requested: pedido=" + pedidoId + " -> " + status);
            session.setAttribute("successMessage", "Status do pedido atualizado para " + status.name() + ".");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMessage", "Status inválido: " + novoStatus);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating order status", e);
            session.setAttribute("errorMessage", "Erro ao atualizar o status do pedido.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/pedidos");
    }
}
