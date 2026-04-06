package br.com.vinheiro.filter;

import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.service.VinheriaService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

// A anotação @WebFilter("/*") indica que este filtro vai interceptar
// TODAS as requisições que chegarem no servidor, sem exceção.
@WebFilter("/*")
public class TenantFilter implements Filter {

    private VinheriaService vinheriaService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Passo 1: O método init roda apenas uma vez quando o servidor sobe.
        // Aqui inicializamos o serviço que vai buscar a vinheria no banco de dados.
        this.vinheriaService = new VinheriaService();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        // Passo 2: Convertemos o request genérico para HttpServletRequest
        // para termos acesso a métodos web, como ler a URL.
        HttpServletRequest request = (HttpServletRequest) req;

        // Passo 3: Chamamos um método auxiliar para descobrir o "slug" (identificador) da vinheria.
        // Exemplo: se a URL for "meusite.com/alcantara-vinhos", o slug é "alcantara-vinhos".
        String slug = extrairSlug(request);

        // Passo 4: Se conseguimos encontrar um slug na URL...
        if (slug != null && !slug.isEmpty()) {
            try {
                // Passo 5: Vamos no banco de dados buscar os dados completos desta vinheria
                Vinheria vinheria = vinheriaService.findBySlug(slug).orElse(null);

                // Passo 6: Se a vinheria existe no banco...
                if (vinheria != null) {
                    // Guardamos o objeto vinheria inteiro E o ID dela dentro da requisição.
                    // Assim, qualquer Servlet ou Service que rodar depois deste filtro
                    // vai saber de qual vinheria estamos falando.
                    request.setAttribute("vinheria", vinheria);
                    request.setAttribute("vinheriaId", vinheria.getId());
                }
            } catch (Exception e) {
                // Se der erro no banco, capturamos aqui para não quebrar a aplicação
                // TODO: No futuro, adicionar um log de erro adequado
            }
        }

        // Passo 7: O comando mais importante do filtro!
        // Ele diz: "Terminei meu trabalho aqui, pode passar a requisição para o próximo filtro ou servlet".
        chain.doFilter(req, resp);
    }

    // Método auxiliar para descobrir qual vinheria o usuário quer acessar
    private String extrairSlug(HttpServletRequest request) {

        // Estratégia 1: Tentar extrair do subdomínio (ex: alcantara.vinheiro.com.br)
        String host = request.getHeader("Host");
        if (host != null && host.contains(".") && !host.startsWith("localhost")) {
            return host.split("\\.")[0]; // Pega a primeira parte antes do ponto
        }

        // Estratégia 2: Tentar extrair do caminho da URL (ex: localhost:8080/alcantara-vinhos/loja)
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        // Divide a URL pelas barras
        String[] parts = path.split("/");

        // Verifica se a URL tem partes suficientes e se NÃO é uma rota de sistema
        // Ignoramos "api", "admin" e "static" porque não são nomes de vinherias
        if (parts.length > 1 && !parts[1].equals("api") && !parts[1].equals("admin") && !parts[1].equals("static")) {
            return parts[1]; // Retorna a primeira palavra após a barra
        }

        // Se não achou em nenhum lugar, retorna nulo
        return null;
    }

    @Override
    public void destroy() {
        // Método executado quando o servidor é desligado.
        // Usado para limpar recursos pesados da memória, se houver.
    }
}