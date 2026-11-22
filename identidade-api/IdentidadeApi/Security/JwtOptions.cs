namespace IdentidadeApi.Security;

/// <summary>
/// Configuração de emissão dos tokens (seção <c>Jwt</c> do appsettings ou variáveis <c>Jwt__*</c>).
/// </summary>
public sealed class JwtOptions
{
    public const string Secao = "Jwt";

    /// <summary>URL pública da Identidade. Deve ser igual à <c>Authority</c> configurada nos serviços que validam o token.</summary>
    public string Issuer { get; set; } = string.Empty;

    /// <summary>Público-alvo do token (serviços da plataforma).</summary>
    public string Audience { get; set; } = string.Empty;

    /// <summary>Validade do token de acesso. Curta de propósito: um token vazado expira rápido.</summary>
    public int ExpiracaoMinutos { get; set; } = 15;

    /// <summary>Validade do refresh token (cookie httpOnly). Cada renovação gera um novo e revoga o anterior.</summary>
    public int RefreshExpiracaoDias { get; set; } = 7;

    /// <summary>
    /// Caminho de um arquivo PEM com a chave privada RSA. Se vazio, uma chave temporária é gerada
    /// na inicialização (os tokens emitidos deixam de valer quando o serviço reinicia).
    /// </summary>
    public string? ChavePrivadaPath { get; set; }
}
