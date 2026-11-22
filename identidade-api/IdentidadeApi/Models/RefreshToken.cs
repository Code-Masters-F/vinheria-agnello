namespace IdentidadeApi.Models;

/// <summary>
/// Refresh token persistido. Só o hash SHA-256 é guardado: se o banco vazar, os valores não podem ser usados.
/// </summary>
public sealed class RefreshToken
{
    public long Id { get; init; }
    public long UsuarioId { get; init; }
    public string TokenHash { get; init; } = string.Empty;
    public DateTime CriadoEm { get; init; }
    public DateTime ExpiraEm { get; init; }
    public DateTime? RevogadoEm { get; init; }

    /// <summary>Hash do token que substituiu este na rotação (rastreabilidade da cadeia de sessões).</summary>
    public string? SubstituidoPorHash { get; init; }
}
