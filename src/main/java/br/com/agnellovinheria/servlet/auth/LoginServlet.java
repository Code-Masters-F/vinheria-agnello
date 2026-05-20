package br.com.agnellovinheria.servlet.auth;

import br.com.agnellovinheria.model.UsuarioAdmin;
import br.com.agnellovinheria.service.UsuarioAdminService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Servlet responsável pelo fluxo de autenticação dos administradores.
 */
@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioAdminService usuarioService;

    public LoginServlet() {
        this.usuarioService = new UsuarioAdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Se já estiver logado, manda direto para o dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioAdmin") != null) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // Caso contrário, exibe a tela de login
        request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            request.setAttribute("error", "E-mail e senha são obrigatórios.");
            request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
            return;
        }

        try {
            System.out.println("[LOGIN] Tentativa de login para: " + email);

            Optional<UsuarioAdmin> optUsuario = usuarioService.autenticar(email, senha);

            if (optUsuario.isPresent()) {
                // Invalidate old session before creating a new one (Session Fixation mitigation)
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("usuarioAdmin", optUsuario.get());

                System.out.println("[LOGIN] Sucesso para: " + email);
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");

            } else {
                System.out.println("[LOGIN] Falha (credenciais inválidas) para: " + email);
                request.setAttribute("error", "E-mail ou senha inválidos.");
                request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
            }

        } catch (Throwable t) {
            // Log the full error so it appears in Render's Logs tab
            System.err.println("[LOGIN] ERRO INESPERADO durante autenticação de '" + email + "'");
            System.err.println("[LOGIN] Tipo: " + t.getClass().getName());
            System.err.println("[LOGIN] Mensagem: " + t.getMessage());
            if (t.getCause() != null) {
                System.err.println("[LOGIN] Causa: " + t.getCause().getClass().getName() + " - " + t.getCause().getMessage());
                if (t.getCause().getCause() != null) {
                    System.err.println("[LOGIN] Causa raiz: " + t.getCause().getCause().getMessage());
                }
            }
            t.printStackTrace(System.err);

            request.setAttribute("error", "Erro interno do servidor: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
        }
    }
}
