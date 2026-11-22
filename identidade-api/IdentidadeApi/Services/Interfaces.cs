using IdentidadeApi.Contracts;
using IdentidadeApi.Models;

namespace IdentidadeApi.Services;

/// <summary>Emissão de tokens JWT assinados.</summary>
public interface ITokenService
{
    TokenEmitido Emitir(Usuario usuario);
}

/// <summary>Validação de credenciais.</summary>
public interface IAutenticacaoService
{
    /// <summary>Retorna o usuário se e-mail e senha forem válidos e ele estiver ativo; caso contrário, <c>null</c>.</summary>
    Task<Usuario?> ValidarCredenciaisAsync(string email, string senha, CancellationToken ct = default);
}

/// <summary>Ciclo de vida da sessão: emissão, renovação (com rotação do refresh token) e encerramento.</summary>
public interface ISessaoService
{
    Task<SessaoEmitida> IniciarAsync(Usuario usuario, CancellationToken ct = default);

    /// <summary>Troca um refresh token válido por uma nova sessão; <c>null</c> se inválido, expirado ou revogado.</summary>
    Task<SessaoEmitida?> RenovarAsync(string refreshToken, CancellationToken ct = default);

    /// <summary>Revoga o refresh token (logout).</summary>
    Task EncerrarAsync(string refreshToken, CancellationToken ct = default);
}
