using System.Security.Cryptography;
using System.Text;
using IdentidadeApi.Contracts;
using IdentidadeApi.Models;
using IdentidadeApi.Repositories;
using IdentidadeApi.Security;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace IdentidadeApi.Services;

/// <summary>
/// Sessões com token de acesso curto (JWT, 15 min) + refresh token opaco (cookie httpOnly, 7 dias).
/// </summary>
/// <remarks>
/// <list type="bullet">
///   <item><b>Opaco e aleatório:</b> 256 bits de <see cref="RandomNumberGenerator"/>; não carrega dados do usuário.</item>
///   <item><b>Só o hash no banco:</b> SHA-256 do valor; um vazamento do banco não permite reutilizar sessões.</item>
///   <item><b>Rotação:</b> cada renovação emite um refresh token novo e revoga o anterior (registrando o substituto).</item>
///   <item><b>Detecção de reuso:</b> apresentar um token já revogado indica roubo (o legítimo e o atacante têm a
///   mesma cópia); todas as sessões do usuário são revogadas e ele precisa logar novamente.</item>
///   <item><b>Estado atual do usuário:</b> a renovação recarrega o usuário; desativado ou removido não renova.</item>
/// </list>
/// </remarks>
public sealed class SessaoService : ISessaoService
{
    private readonly ITokenService _tokens;
    private readonly IRefreshTokenRepository _refreshTokens;
    private readonly IUsuarioRepository _usuarios;
    private readonly TimeProvider _relogio;
    private readonly JwtOptions _opcoes;
    private readonly ILogger<SessaoService> _logger;

    public SessaoService(ITokenService tokens, IRefreshTokenRepository refreshTokens, IUsuarioRepository usuarios,
        TimeProvider relogio, IOptions<JwtOptions> opcoes, ILogger<SessaoService> logger)
    {
        _tokens = tokens;
        _refreshTokens = refreshTokens;
        _usuarios = usuarios;
        _relogio = relogio;
        _opcoes = opcoes.Value;
        _logger = logger;
    }

    public async Task<SessaoEmitida> IniciarAsync(Usuario usuario, CancellationToken ct = default)
    {
        var (valor, registro) = NovoRefreshToken(usuario.Id);
        // [TokenOptimization] - Uma única gravação por login (somente o hash do refresh token).
        await _refreshTokens.SalvarAsync(registro, ct);
        return new SessaoEmitida(_tokens.Emitir(usuario), valor, registro.ExpiraEm);
    }

    public async Task<SessaoEmitida?> RenovarAsync(string refreshToken, CancellationToken ct = default)
    {
        if (string.IsNullOrWhiteSpace(refreshToken)) return null;

        var agora = Agora();
        var atual = await _refreshTokens.BuscarPorHashAsync(Hash(refreshToken), ct);
        if (atual is null) return null;

        if (atual.RevogadoEm is not null)
        {
            await RevogarTudoPorReusoAsync(atual.UsuarioId, agora, ct);
            return null;
        }

        if (atual.ExpiraEm <= agora) return null;

        var usuario = await _usuarios.BuscarPorIdAsync(atual.UsuarioId, ct);
        if (usuario is null || !usuario.Ativo)
        {
            await _refreshTokens.RevogarAsync(atual.TokenHash, agora, null, ct);
            return null;
        }

        var (valor, novo) = NovoRefreshToken(usuario.Id);
        // Revogação condicional (só se ainda ativo): se outra requisição usou o mesmo token ao mesmo tempo,
        // apenas uma vence; a outra é tratada como reuso.
        if (!await _refreshTokens.RevogarAsync(atual.TokenHash, agora, novo.TokenHash, ct))
        {
            await RevogarTudoPorReusoAsync(atual.UsuarioId, agora, ct);
            return null;
        }
        await _refreshTokens.SalvarAsync(novo, ct);

        _logger.LogInformation("Sessão renovada para o usuário {UsuarioId}", usuario.Id);
        return new SessaoEmitida(_tokens.Emitir(usuario), valor, novo.ExpiraEm);
    }

    public async Task EncerrarAsync(string refreshToken, CancellationToken ct = default)
    {
        if (string.IsNullOrWhiteSpace(refreshToken)) return;
        // [TokenOptimization] - Revogação direta pelo hash, sem consulta prévia.
        await _refreshTokens.RevogarAsync(Hash(refreshToken), Agora(), null, ct);
    }

    private async Task RevogarTudoPorReusoAsync(long usuarioId, DateTime agora, CancellationToken ct)
    {
        _logger.LogWarning("Reuso de refresh token revogado para o usuário {UsuarioId}: possível roubo de sessão. " +
                           "Todas as sessões do usuário foram revogadas.", usuarioId);
        await _refreshTokens.RevogarTodosDoUsuarioAsync(usuarioId, agora, ct);
    }

    private DateTime Agora() => _relogio.GetUtcNow().UtcDateTime;

    private (string Valor, RefreshToken Registro) NovoRefreshToken(long usuarioId)
    {
        var valor = Base64UrlEncoder.Encode(RandomNumberGenerator.GetBytes(32));
        var agora = Agora();
        return (valor, new RefreshToken
        {
            UsuarioId = usuarioId,
            TokenHash = Hash(valor),
            CriadoEm = agora,
            ExpiraEm = agora.AddDays(_opcoes.RefreshExpiracaoDias),
        });
    }

    private static string Hash(string valor) =>
        Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(valor))).ToLowerInvariant();
}
