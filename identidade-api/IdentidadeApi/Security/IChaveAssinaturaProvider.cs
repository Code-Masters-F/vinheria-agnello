using Microsoft.IdentityModel.Tokens;

namespace IdentidadeApi.Security;

/// <summary>
/// Fornece o par de chaves RSA usado para assinar os tokens (RS256).
/// A chave privada nunca sai da Identidade; a pública é publicada no JWKS para os demais serviços.
/// </summary>
public interface IChaveAssinaturaProvider
{
    RsaSecurityKey ChavePrivada { get; }
    RsaSecurityKey ChavePublica { get; }

    /// <summary>Identificador da chave (<c>kid</c>), presente no header do token e no JWKS.</summary>
    string KeyId { get; }
}
