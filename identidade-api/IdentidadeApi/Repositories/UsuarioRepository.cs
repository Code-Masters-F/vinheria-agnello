using Dapper;
using IdentidadeApi.Models;
using Npgsql;

namespace IdentidadeApi.Repositories;

/// <summary>Implementação PostgreSQL (Dapper + Npgsql) do repositório de usuários.</summary>
public sealed class UsuarioRepository : IUsuarioRepository
{
    // Consulta parametrizada (sem concatenação de strings) para evitar SQL Injection.
    private const string SqlBuscarPorEmail = """
        SELECT id          AS Id,
               vinheria_id AS VinheriaId,
               nome        AS Nome,
               email       AS Email,
               senha_hash  AS SenhaHash,
               perfil      AS Perfil,
               ativo       AS Ativo
          FROM usuario
         WHERE lower(email) = lower(@Email)
         LIMIT 1
        """;

    private const string SqlBuscarPorId = """
        SELECT id AS Id, vinheria_id AS VinheriaId, nome AS Nome, email AS Email,
               senha_hash AS SenhaHash, perfil AS Perfil, ativo AS Ativo
          FROM usuario
         WHERE id = @Id
        """;

    private readonly string _connectionString;

    public UsuarioRepository(string connectionString)
    {
        _connectionString = connectionString;
    }

    public async Task<Usuario?> BuscarPorEmailAsync(string email, CancellationToken ct = default)
    {
        // [TokenOptimization] - Conexão obtida do pool do Npgsql e devolvida logo após a consulta.
        await using var conexao = new NpgsqlConnection(_connectionString);
        return await conexao.QuerySingleOrDefaultAsync<Usuario>(
            new CommandDefinition(SqlBuscarPorEmail, new { Email = email }, cancellationToken: ct));
    }

    public async Task<Usuario?> BuscarPorIdAsync(long id, CancellationToken ct = default)
    {
        // [TokenOptimization] - Consulta pela chave primária, devolvendo a conexão ao pool logo em seguida.
        await using var conexao = new NpgsqlConnection(_connectionString);
        return await conexao.QuerySingleOrDefaultAsync<Usuario>(
            new CommandDefinition(SqlBuscarPorId, new { Id = id }, cancellationToken: ct));
    }
}
