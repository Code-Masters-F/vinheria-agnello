using IdentidadeApi.Contracts;
using IdentidadeApi.Services;

namespace IdentidadeApi.Endpoints;

/// <summary>Rotas públicas de autenticação e sessão (<c>/api/v1/identidade</c>).</summary>
/// <remarks>
/// O token de acesso (JWT, 15 min) vai no corpo da resposta e é enviado pelo cliente no header
/// <c>Authorization: Bearer</c>. O refresh token vai <b>somente</b> no cookie <see cref="CookieRefresh"/>,
/// inacessível ao JavaScript (httpOnly), enviado só por HTTPS (Secure), nunca em requisições de outros
/// sites (SameSite=Strict, que também protege /refresh e /logout contra CSRF) e restrito a estas rotas.
/// </remarks>
public static class IdentidadeEndpoints
{
    public static IEndpointRouteBuilder MapIdentidadeEndpoints(this IEndpointRouteBuilder app)
    {
        var grupo = app.MapGroup(CookieRefresh.Caminho).WithTags("Identidade");

        grupo.MapPost("/login", LoginAsync)
            .WithName("Login")
            .WithOpenApi()
            .Produces<LoginResponse>()
            .ProducesValidationProblem()
            .ProducesProblem(StatusCodes.Status401Unauthorized);

        grupo.MapPost("/refresh", RenovarAsync)
            .WithName("RenovarSessao")
            .WithOpenApi()
            .Produces<LoginResponse>()
            .ProducesProblem(StatusCodes.Status401Unauthorized);

        grupo.MapPost("/logout", EncerrarAsync)
            .WithName("Logout")
            .WithOpenApi()
            .Produces(StatusCodes.Status204NoContent);

        return app;
    }

    /// <summary>
    /// 200 com o token de acesso (+ cookie de refresh) quando as credenciais são válidas; 400 se faltar
    /// e-mail/senha; 401 com mensagem genérica em qualquer outra falha.
    /// </summary>
    private static async Task<IResult> LoginAsync(LoginRequest requisicao, IAutenticacaoService autenticacao,
        ISessaoService sessoes, HttpResponse resposta, CancellationToken ct)
    {
        var erros = new Dictionary<string, string[]>();
        if (string.IsNullOrWhiteSpace(requisicao.Email)) erros["email"] = new[] { "Informe o e-mail." };
        if (string.IsNullOrWhiteSpace(requisicao.Senha)) erros["senha"] = new[] { "Informe a senha." };
        if (erros.Count > 0) return Results.ValidationProblem(erros);

        // [TokenOptimization] - Delegação direta aos serviços, sem consultas extras no endpoint.
        var usuario = await autenticacao.ValidarCredenciaisAsync(requisicao.Email!, requisicao.Senha!, ct);
        if (usuario is null)
        {
            return Results.Problem(statusCode: StatusCodes.Status401Unauthorized,
                                   title: "Credenciais inválidas",
                                   detail: "E-mail ou senha incorretos.");
        }

        var sessao = await sessoes.IniciarAsync(usuario, ct);
        CookieRefresh.Gravar(resposta, sessao);
        return Results.Ok(ParaResposta(sessao));
    }

    /// <summary>Troca o refresh token do cookie por um novo token de acesso (e um novo refresh token).</summary>
    private static async Task<IResult> RenovarAsync(HttpRequest requisicao, HttpResponse resposta,
        ISessaoService sessoes, CancellationToken ct)
    {
        var valor = requisicao.Cookies[CookieRefresh.Nome];
        var sessao = string.IsNullOrEmpty(valor) ? null : await sessoes.RenovarAsync(valor, ct);

        if (sessao is null)
        {
            CookieRefresh.Apagar(resposta);
            return Results.Problem(statusCode: StatusCodes.Status401Unauthorized,
                                   title: "Sessão inválida ou expirada",
                                   detail: "Faça login novamente.");
        }

        CookieRefresh.Gravar(resposta, sessao);
        return Results.Ok(ParaResposta(sessao));
    }

    /// <summary>Revoga o refresh token do cookie e apaga o cookie.</summary>
    private static async Task<IResult> EncerrarAsync(HttpRequest requisicao, HttpResponse resposta,
        ISessaoService sessoes, CancellationToken ct)
    {
        var valor = requisicao.Cookies[CookieRefresh.Nome];
        if (!string.IsNullOrEmpty(valor)) await sessoes.EncerrarAsync(valor, ct);

        CookieRefresh.Apagar(resposta);
        return Results.NoContent();
    }

    private static LoginResponse ParaResposta(SessaoEmitida sessao) =>
        new(sessao.Acesso.AccessToken, "Bearer", sessao.Acesso.ExpiraEmSegundos);
}

/// <summary>Cookie do refresh token: httpOnly, Secure, SameSite=Strict, restrito às rotas da Identidade.</summary>
public static class CookieRefresh
{
    public const string Nome = "vinheria_refresh";
    public const string Caminho = "/api/v1/identidade";

    public static void Gravar(HttpResponse resposta, SessaoEmitida sessao) =>
        resposta.Cookies.Append(Nome, sessao.RefreshToken, Opcoes(sessao.RefreshExpiraEm));

    public static void Apagar(HttpResponse resposta) =>
        resposta.Cookies.Delete(Nome, Opcoes(null));

    private static CookieOptions Opcoes(DateTime? expiraEm) => new()
    {
        HttpOnly = true,
        Secure = true,
        SameSite = SameSiteMode.Strict,
        Path = Caminho,
        Expires = expiraEm,
        IsEssential = true,
    };
}
