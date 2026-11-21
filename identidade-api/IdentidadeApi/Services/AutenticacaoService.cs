using IdentidadeApi.Contracts;
using IdentidadeApi.Models;
using IdentidadeApi.Repositories;

namespace IdentidadeApi.Services;

/// <summary>
/// Valida e-mail e senha (hash BCrypt) e emite o token de acesso.
/// </summary>
/// <remarks>
/// Todas as falhas (usuário inexistente, inativo, senha errada, hash corrompido) retornam <c>null</c>,
/// sem distinguir o motivo para quem chamou — isso evita enumeração de usuários. Quando o usuário não
/// existe, ainda é feita uma verificação BCrypt contra um hash fictício para que o tempo de resposta
/// seja parecido com o de uma senha errada (mitiga ataque de timing).
/// </remarks>
public sealed class AutenticacaoService : IAutenticacaoService
{
    // Gerado uma vez por processo; custo 10, o mesmo dos hashes reais do banco.
    private static readonly string HashFicticio = BCrypt.Net.BCrypt.HashPassword(Guid.NewGuid().ToString(), 10);

    private readonly IUsuarioRepository _repositorio;
    private readonly ITokenService _tokens;
    private readonly ILogger<AutenticacaoService> _logger;

    public AutenticacaoService(IUsuarioRepository repositorio, ITokenService tokens, ILogger<AutenticacaoService> logger)
    {
        _repositorio = repositorio;
        _tokens = tokens;
        _logger = logger;
    }

    public async Task<TokenEmitido?> AutenticarAsync(string email, string senha, CancellationToken ct = default)
    {
        // [TokenOptimization] - Uma única consulta ao banco por tentativa de login.
        var usuario = await _repositorio.BuscarPorEmailAsync(email.Trim(), ct);

        if (usuario is null || !usuario.Ativo)
        {
            BCrypt.Net.BCrypt.Verify(senha, HashFicticio);
            _logger.LogInformation("Login recusado: usuário inexistente ou inativo");
            return null;
        }

        if (!SenhaConfere(senha, usuario))
        {
            _logger.LogInformation("Login recusado para o usuário {UsuarioId}: senha incorreta", usuario.Id);
            return null;
        }

        _logger.LogInformation("Login bem-sucedido para o usuário {UsuarioId} ({Perfil})", usuario.Id, usuario.Perfil);
        return _tokens.Emitir(usuario);
    }

    private bool SenhaConfere(string senha, Usuario usuario)
    {
        try
        {
            // [TokenOptimization] - Verificação direta contra o hash armazenado, sem re-hash adicional.
            return BCrypt.Net.BCrypt.Verify(senha, usuario.SenhaHash);
        }
        catch (Exception ex) when (ex is not OperationCanceledException)
        {
            // Hash inválido no banco: falha fechada (nega o acesso) em vez de erro 500.
            _logger.LogError(ex, "Hash de senha inválido para o usuário {UsuarioId}", usuario.Id);
            return false;
        }
    }
}
