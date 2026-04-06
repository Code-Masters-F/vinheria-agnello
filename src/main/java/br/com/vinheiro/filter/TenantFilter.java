package br.com.vinheiro.filter;

import br.com.vinheiro.model.Vinheria;
import br.com.vinheiro.service.VinheriaService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

// Registrado e ordenado via web.xml para garantir que execute antes de AuthFilter.
// A anotação @WebFilter foi removida para evitar registro duplicado e ordem indefinida.
public class TenantFilter implements Filter {

    // Rotas de sistema que nunca devem ser tratadas como slugs de vinheria
    private static final Set<String> RESERVED_SEGMENTS = Set.of(
        "api", "admin", "static", "auth", "login", "registro", "error"
    );

    // Domínios pai permitidos para extração de subdomínio.
    // Apenas hosts cujo sufixo bate com um destes são tratados como subdomínio-slug.
    private static final Set<String> PARENT_DOMAINS = Set.of(
        "vinheiro.com.br",
        "vinheria-agnello.com.br"
    );

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
        HttpServletResponse response = (HttpServletResponse) resp;

        // Passo 3: Chamamos um método auxiliar para descobrir o "slug" (identificador) da vinheria.
        // Exemplo: se a URL for "meusite.com/alcantara-vinhos", o slug é "alcantara-vinhos".
        String slug = extrairSlug(request);

        // Passo 4: Se conseguimos encontrar um slug na URL...
        if (slug != null && !slug.isEmpty()) {
            try {
                // Passo 5: Vamos no banco de dados buscar os dados completos desta vinheria
                // Usamos findBySlugActive para ignorar tenants inativos na resolução pública.
                java.util.Optional<Vinheria> opt = vinheriaService.findBySlugActive(slug);

                // Passo 6: Se a vinheria não existe ou está inativa, retornamos 404
                if (opt.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Vinheria não encontrada: " + slug);
                    return;
                }

                Vinheria vinheria = opt.get();
                // Guardamos o objeto vinheria inteiro E o ID dela dentro da requisição.
                // Assim, qualquer Servlet ou Service que rodar depois deste filtro
                // vai saber de qual vinheria estamos falando.
                request.setAttribute("vinheria", vinheria);
                request.setAttribute("vinheriaId", vinheria.getId());

            } catch (Exception e) {
                // Se der erro no banco, retornamos 500 em vez de engolir a exceção
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao resolver tenant");
                return;
            }
        }

        // Passo 7: O comando mais importante do filtro!
        // Ele diz: "Terminei meu trabalho aqui, pode passar a requisição para o próximo filtro ou servlet".
        chain.doFilter(req, resp);
    }

    // Método auxiliar para descobrir qual vinheria o usuário quer acessar
    private String extrairSlug(HttpServletRequest request) {

        // Estratégia 1: Tentar extrair do subdomínio (ex: alcantara.vinheiro.com.br)
        // Só aplica quando o host pertence a um domínio pai conhecido e não é IP/localhost.
        String host = request.getServerName();
        if (host != null && !isIpOrLocalhost(host)) {
            for (String parent : PARENT_DOMAINS) {
                if (host.endsWith("." + parent)) {
                    String subdomain = host.substring(0, host.length() - parent.length() - 1);
                    // Subdomain deve ser simples (sem pontos) e não reservado
                    if (!subdomain.contains(".") && !RESERVED_SEGMENTS.contains(subdomain)) {
                        return subdomain;
                    }
                }
            }
        }

        // Estratégia 2: Tentar extrair do caminho da URL (ex: localhost:8080/alcantara-vinhos/loja)
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        // Divide a URL pelas barras
        String[] parts = path.split("/");

        // Verifica se a URL tem partes suficientes e se NÃO é uma rota de sistema reservada
        if (parts.length > 1 && !parts[1].isEmpty() && !RESERVED_SEGMENTS.contains(parts[1])) {
            return parts[1]; // Retorna a primeira palavra após a barra
        }

        // Se não achou em nenhum lugar, retorna nulo
        return null;
    }

    private boolean isIpOrLocalhost(String host) {
        if ("localhost".equalsIgnoreCase(host)) {
            return true;
        }
        // Detecta endereços IPv4 (apenas dígitos e pontos) ou IPv6 (contém ":")
        return host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || host.contains(":");
    }

    @Override
    public void destroy() {
        // Método executado quando o servidor é desligado.
        // Usado para limpar recursos pesados da memória, se houver.
    }
}