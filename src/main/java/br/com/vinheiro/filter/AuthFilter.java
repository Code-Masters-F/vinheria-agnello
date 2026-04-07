package br.com.vinheiro.filter;

import br.com.vinheiro.model.UsuarioAdmin;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Registrado e ordenado via web.xml após TenantFilter.
// A anotação @WebFilter foi removida para evitar registro duplicado e ordem indefinida.
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nada a inicializar neste filtro
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        // Passo 1: Conversão para acessar métodos HTTP (como ler sessão e fazer redirecionamento)
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // Passo 2: Pegamos a URL exata que o usuário está tentando acessar
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Passo 3: Exceções de Segurança!
        // Telas de login, registro e arquivos visuais (CSS/JS) precisam ser públicas.
        // Usamos comparações exatas e prefixo estrito para evitar expor rotas protegidas.
        String adminLogin = contextPath + "/admin/login.jsp";
        String adminRegistro = contextPath + "/admin/registro.jsp";
        String staticPrefix = contextPath + "/admin/static/";
        if (uri.equals(adminLogin) || uri.equals(adminRegistro) || uri.startsWith(staticPrefix)) {
            chain.doFilter(req, resp); // Libera o acesso
            return; // Para a execução do filtro aqui
        }

        // Passo 4: Verificação de Login
        // O "false" significa: "Me dê a sessão atual. Se não existir, NÃO crie uma nova (retorne null)".
        HttpSession session = request.getSession(false);

        // Tentamos pegar o objeto "usuarioAdmin" de dentro da sessão
        UsuarioAdmin usuarioAdmin = (session != null) ? (UsuarioAdmin) session.getAttribute("usuarioAdmin") : null;

        // Passo 5: Se o usuário NÃO estiver logado (sessão ou usuário nulos)...
        if (usuarioAdmin == null) {
            // Expulsamos o usuário enviando-o para a página de login
            response.sendRedirect(contextPath + "/auth/login.jsp");
            return; // Para a execução
        }

        // Passo 6: Segurança Multi-tenant (Isolamento de Dados)
        // O TenantFilter (que rodou antes) colocou o ID da vinheria da URL na requisição. Vamos pegá-lo.
        Long requestVinheriaId = (Long) request.getAttribute("vinheriaId");

        // Se a URL tem uma vinheria E o administrador logado pertence a uma vinheria...
        if (requestVinheriaId != null && usuarioAdmin.getVinheria() != null) {

            // Pegamos o ID da vinheria que este administrador é dono
            Long adminVinheriaId = usuarioAdmin.getVinheria().getId();

            // Passo 7: Se a vinheria que ele quer acessar é DIFERENTE da vinheria da qual ele é dono...
            if (!requestVinheriaId.equals(adminVinheriaId)) {
                // Bloqueamos com erro 403 (Proibido)
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar o painel de outra Vinheria.");
                return; // Para a execução
            }
        }

        // Passo 8: Se passou por todas as barreiras (está logado e é o dono certo),
        // liberamos o acesso para ele ver a página do Admin.
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}