using IdentidadeApi.Contracts;
using IdentidadeApi.Services;

namespace IdentidadeApi.Endpoints;

/// <summary>Rotas públicas de autenticação (<c>/api/v1/identidade</c>).</summary>
public static class IdentidadeEndpoints
{
    public static IEndpointRouteBuilder MapIdentidadeEndpoints(this IEndpointRouteBuilder app)
    {
        var grupo = app.MapGroup("/api/v1/identidade").WithTags("Identidade");

        grupo.MapPost("/login", LoginAsync)
            .WithName("Login")
            .WithOpenApi()
            .Produces<LoginResponse>()
            .ProducesValidationProblem()
            .ProducesProblem(StatusCodes.Status401Unauthorized);

        return app;
    }

    /// <summary>
    /// 200 com o token quando as credenciais são válidas; 400 se faltar e-mail/senha;
    /// 401 com mensagem genérica em qualquer outra falha.
    /// </summary>
    private static async Task<IResult> LoginAsync(LoginRequest requisicao, IAutenticacaoService autenticacao, CancellationToken ct)
    {
        var erros = new Dictionary<string, string[]>();
        if (string.IsNullOrWhiteSpace(requisicao.Email)) erros["email"] = new[] { "Informe o e-mail." };
        if (string.IsNullOrWhiteSpace(requisicao.Senha)) erros["senha"] = new[] { "Informe a senha." };
        if (erros.Count > 0) return Results.ValidationProblem(erros);

        // [TokenOptimization] - Delegação direta ao serviço de autenticação, sem consultas extras no endpoint.
        var token = await autenticacao.AutenticarAsync(requisicao.Email!, requisicao.Senha!, ct);

        return token is null
            ? Results.Problem(statusCode: StatusCodes.Status401Unauthorized,
                              title: "Credenciais inválidas",
                              detail: "E-mail ou senha incorretos.")
            : Results.Ok(new LoginResponse(token.AccessToken, "Bearer", token.ExpiraEmSegundos));
    }
}
