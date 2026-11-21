using IdentidadeApi.Contracts;
using IdentidadeApi.Models;

namespace IdentidadeApi.Services;

/// <summary>Emissão de tokens JWT assinados.</summary>
public interface ITokenService
{
    TokenEmitido Emitir(Usuario usuario);
}

/// <summary>Validação de credenciais e emissão do token de acesso.</summary>
public interface IAutenticacaoService
{
    /// <summary>Retorna o token se e-mail e senha forem válidos e o usuário estiver ativo; caso contrário, <c>null</c>.</summary>
    Task<TokenEmitido?> AutenticarAsync(string email, string senha, CancellationToken ct = default);
}
