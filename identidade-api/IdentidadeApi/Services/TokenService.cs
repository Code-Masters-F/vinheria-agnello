using System.Globalization;
using IdentidadeApi.Contracts;
using IdentidadeApi.Models;
using IdentidadeApi.Security;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Tokens;

namespace IdentidadeApi.Services;

/// <summary>
/// Emite tokens JWT assinados com RS256.
/// </summary>
/// <remarks>
/// Claims emitidas:
/// <list type="bullet">
///   <item><c>sub</c> — id do usuário; <c>email</c>; <c>name</c>;</item>
///   <item><c>role</c> — perfil (Admin/Cliente), usado no RBAC de cada microsserviço;</item>
///   <item><c>vinheria_id</c> — tenant do usuário (isolamento multi-tenant);</item>
///   <item><c>jti</c> — id único do token; <c>iss</c>/<c>aud</c>/<c>iat</c>/<c>nbf</c>/<c>exp</c>.</item>
/// </list>
/// A senha e o hash nunca entram no token.
/// </remarks>
public sealed class TokenService : ITokenService
{
    private readonly JsonWebTokenHandler _handler = new();
    private readonly IChaveAssinaturaProvider _chaves;
    private readonly JwtOptions _opcoes;
    private readonly TimeProvider _relogio;

    public TokenService(IChaveAssinaturaProvider chaves, IOptions<JwtOptions> opcoes, TimeProvider relogio)
    {
        _chaves = chaves;
        _opcoes = opcoes.Value;
        _relogio = relogio;
    }

    public TokenEmitido Emitir(Usuario usuario)
    {
        var agora = _relogio.GetUtcNow().UtcDateTime;
        var validade = TimeSpan.FromMinutes(_opcoes.ExpiracaoMinutos);

        var descritor = new SecurityTokenDescriptor
        {
            Issuer = _opcoes.Issuer,
            Audience = _opcoes.Audience,
            IssuedAt = agora,
            NotBefore = agora,
            Expires = agora.Add(validade),
            SigningCredentials = new SigningCredentials(_chaves.ChavePrivada, SecurityAlgorithms.RsaSha256),
            Claims = new Dictionary<string, object>
            {
                [JwtRegisteredClaimNames.Sub] = usuario.Id.ToString(CultureInfo.InvariantCulture),
                [JwtRegisteredClaimNames.Email] = usuario.Email,
                [JwtRegisteredClaimNames.Name] = usuario.Nome,
                [JwtRegisteredClaimNames.Jti] = Guid.NewGuid().ToString("N"),
                ["role"] = usuario.Perfil,
                ["vinheria_id"] = usuario.VinheriaId,
            },
        };

        // [TokenOptimization] - Handler reutilizado (thread-safe) em vez de instanciado a cada emissão.
        return new TokenEmitido(_handler.CreateToken(descritor), (int)validade.TotalSeconds);
    }
}
