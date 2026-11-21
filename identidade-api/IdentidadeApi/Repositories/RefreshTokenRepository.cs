using Dapper;
using IdentidadeApi.Models;
using Npgsql;

namespace IdentidadeApi.Repositories;

/// <summary>Implementação PostgreSQL (Dapper + Npgsql) dos refresh tokens. Consultas parametrizadas.</summary>
public sealed class RefreshTokenRepository : IRefreshTokenRepository
{
    private const string SqlSalvar = """
        INSERT INTO refresh_token (usuario_id, token_hash, criado_em, expira_em)
        VALUES (@UsuarioId, @TokenHash, @CriadoEm, @ExpiraEm)
        """;

    private const string SqlBuscarPorHash = """
        SELECT id                   AS Id,
               usuario_id           AS UsuarioId,
               token_hash           AS TokenHash,
               criado_em            AS CriadoEm,
               expira_em            AS ExpiraEm,
               revogado_em          AS RevogadoEm,
               substituido_por_hash AS SubstituidoPorHash
          FROM refresh_token
         WHERE token_hash = @TokenHash
        """;

    // "revogado_em IS NULL" torna a revogação atômica: só uma requisição consegue rotacionar o mesmo token.
    private const string SqlRevogar = """
        UPDATE refresh_token
           SET revogado_em = @Quando, substituido_por_hash = @SubstituidoPorHash
         WHERE token_hash = @TokenHash AND revogado_em IS NULL
        """;

    private const string SqlRevogarTodosDoUsuario = """
        UPDATE refresh_token
           SET revogado_em = @Quando
         WHERE usuario_id = @UsuarioId AND revogado_em IS NULL
        """;

    private readonly string _connectionString;

    public RefreshTokenRepository(string connectionString)
    {
        _connectionString = connectionString;
    }

    public async Task SalvarAsync(RefreshToken token, CancellationToken ct = default)
    {
        // [TokenOptimization] - Conexão do pool devolvida logo após cada comando.
        await using var conexao = new NpgsqlConnection(_connectionString);
        await conexao.ExecuteAsync(new CommandDefinition(SqlSalvar, token, cancellationToken: ct));
    }

    public async Task<RefreshToken?> BuscarPorHashAsync(string tokenHash, CancellationToken ct = default)
    {
        await using var conexao = new NpgsqlConnection(_connectionString);
        return await conexao.QuerySingleOrDefaultAsync<RefreshToken>(
            new CommandDefinition(SqlBuscarPorHash, new { TokenHash = tokenHash }, cancellationToken: ct));
    }

    public async Task<bool> RevogarAsync(string tokenHash, DateTime quando, string? substituidoPorHash, CancellationToken ct = default)
    {
        await using var conexao = new NpgsqlConnection(_connectionString);
        var alterados = await conexao.ExecuteAsync(new CommandDefinition(SqlRevogar,
            new { TokenHash = tokenHash, Quando = quando, SubstituidoPorHash = substituidoPorHash }, cancellationToken: ct));
        return alterados > 0;
    }

    public async Task RevogarTodosDoUsuarioAsync(long usuarioId, DateTime quando, CancellationToken ct = default)
    {
        await using var conexao = new NpgsqlConnection(_connectionString);
        await conexao.ExecuteAsync(new CommandDefinition(SqlRevogarTodosDoUsuario,
            new { UsuarioId = usuarioId, Quando = quando }, cancellationToken: ct));
    }
}
