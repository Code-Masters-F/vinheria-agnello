package br.com.agnellovinheria.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

// A anotação @WebFilter("/api/*") indica que este filtro SÓ roda
// para as rotas que retornam dados JSON (nossa API REST para o Kotlin).
@WebFilter("/api/*")
public class CorsFilter implements Filter {

    // Origens confiáveis que podem enviar credenciais.
    // Adicione aqui os domínios permitidos em produção.
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
        "http://localhost:3000",
        "http://localhost:8080"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // Passo 1: Configurar as permissões (Headers CORS)

        // Valida a origem contra a lista de origens confiáveis.
        // Usar "*" junto com Allow-Credentials é inválido, portanto refletimos
        // a origem somente quando ela for reconhecida.
        String origin = request.getHeader("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }

        // Diz quais tipos de ação HTTP são permitidas
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // Diz quais cabeçalhos especiais o aplicativo Kotlin pode enviar para nós
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Admin-Token");

        // Diz para o navegador memorizar essas regras por 1 hora (3600 segundos) para não ficar perguntando toda hora
        response.setHeader("Access-Control-Max-Age", "3600");

        // Passo 2: O tratamento do "Preflight" (Voo de reconhecimento)
        // Antes de fazer um POST ou PUT, o navegador muitas vezes manda uma requisição invisível
        // do tipo "OPTIONS" só para perguntar: "Ei servidor, você aceita meu POST?".
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // Se for só uma pergunta (OPTIONS), nós respondemos com Status 200 (OK)
            // e encerramos aqui. Não precisamos processar a lógica da API.
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Passo 3: Se for um GET, POST, etc normal, liberamos a requisição
        // para chegar nos nossos Servlets da API.
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}