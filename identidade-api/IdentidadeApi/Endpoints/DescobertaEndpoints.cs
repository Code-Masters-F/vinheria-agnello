using IdentidadeApi.Security;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace IdentidadeApi.Endpoints;

/// <summary>
/// Endpoints de descoberta usados pelos outros microsserviços para validar os tokens.
/// </summary>
/// <remarks>
/// O Catálogo configura <c>Authority = Issuer</c> no JWT Bearer do ASP.NET Core. O middleware lê
/// <c>/.well-known/openid-configuration</c>, segue o <c>jwks_uri</c> e baixa a chave pública — essa é a
/// comunicação entre os dois serviços. Se o <c>kid</c> de um token não estiver no cache, o JWKS é buscado de novo.
/// </remarks>
public static class DescobertaEndpoints
{
    public static IEndpointRouteBuilder MapDescobertaEndpoints(this IEndpointRouteBuilder app)
    {
        var grupo = app.MapGroup("/.well-known").WithTags("Descoberta");

        grupo.MapGet("/openid-configuration", (IOptions<JwtOptions> opcoes) =>
        {
            var issuer = opcoes.Value.Issuer.TrimEnd('/');
            // [TokenOptimization] - Documento pequeno e estático, montado sem consultar banco.
            return Results.Ok(new
            {
                issuer,
                jwks_uri = $"{issuer}/.well-known/jwks.json",
                id_token_signing_alg_values_supported = new[] { SecurityAlgorithms.RsaSha256 },
                subject_types_supported = new[] { "public" },
                claims_supported = new[] { "sub", "email", "name", "role", "vinheria_id" },
            });
        }).WithName("OpenIdConfiguration");

        grupo.MapGet("/jwks.json", (IChaveAssinaturaProvider chaves) =>
        {
            // Somente os parâmetros públicos (n, e). A chave privada nunca é serializada.
            var parametros = chaves.ChavePublica.Parameters;
            // [TokenOptimization] - Reaproveita a chave pública já carregada pelo provider singleton.
            return Results.Ok(new
            {
                keys = new[]
                {
                    new
                    {
                        kty = "RSA",
                        use = "sig",
                        alg = SecurityAlgorithms.RsaSha256,
                        kid = chaves.KeyId,
                        n = Base64UrlEncoder.Encode(parametros.Modulus),
                        e = Base64UrlEncoder.Encode(parametros.Exponent),
                    },
                },
            });
        }).WithName("Jwks");

        return app;
    }
}
