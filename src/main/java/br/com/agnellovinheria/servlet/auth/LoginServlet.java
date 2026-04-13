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

        Optional<UsuarioAdmin> optUsuario = usuarioService.autenticar(email, senha);

        if (optUsuario.isPresent()) {
            // Fix: Invalidate old session before creating a new one (Session Fixation mitigation)
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            
            // Create a brand new session and store user data
            HttpSession session = request.getSession(true);
            session.setAttribute("usuarioAdmin", optUsuario.get());
            
            // Redireciona para o Dashboard principal
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            // Falha na autenticação
            request.setAttribute("error", "E-mail ou senha inválidos.");
            request.getRequestDispatcher("/WEB-INF/views/admin/login.jsp").forward(request, response);
        }
    }
}
