using IdentidadeApi.Models;

namespace IdentidadeApi.Repositories;

/// <summary>Persistência dos refresh tokens (somente hashes).</summary>
public interface IRefreshTokenRepository
{
    Task SalvarAsync(RefreshToken token, CancellationToken ct = default);
    Task<RefreshToken?> BuscarPorHashAsync(string tokenHash, CancellationToken ct = default);

    /// <summary>
    /// Revoga o token se ele ainda estiver ativo, registrando qual token o substituiu na rotação.
    /// Retorna <c>false</c> se ele já estava revogado (operação atômica: em uso concorrente, só uma renovação vence).
    /// </summary>
    Task<bool> RevogarAsync(string tokenHash, DateTime quando, string? substituidoPorHash, CancellationToken ct = default);

    /// <summary>
    /// Rotação atômica: revoga <paramref name="tokenHashAtual"/> (se ainda ativo) e grava o <paramref name="novo"/>
    /// na mesma transação. Retorna <c>false</c> sem gravar nada se o token atual já estava revogado.
    /// </summary>
    Task<bool> RotacionarAsync(string tokenHashAtual, RefreshToken novo, DateTime quando, CancellationToken ct = default);

    /// <summary>Revoga todas as sessões ativas do usuário (usado ao detectar reuso de token).</summary>
    Task RevogarTodosDoUsuarioAsync(long usuarioId, DateTime quando, CancellationToken ct = default);
}
