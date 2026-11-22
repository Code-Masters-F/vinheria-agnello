using System.Security.Cryptography;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace IdentidadeApi.Security;

/// <summary>
/// Carrega (ou gera) o par de chaves RSA de assinatura dos tokens.
/// </summary>
/// <remarks>
/// <para>
/// Com <see cref="JwtOptions.ChavePrivadaPath"/> configurado, a chave privada é lida de um arquivo PEM
/// montado no container (fora do repositório). Sem ele, gera-se uma chave temporária em memória — útil em
/// desenvolvimento, mas os tokens deixam de valer a cada reinício.
/// </para>
/// <para>
/// O <c>kid</c> é o thumbprint RFC 7638 da chave pública: muda automaticamente quando a chave muda, o que
/// permite aos serviços consumidores detectar a troca e buscar o JWKS novamente.
/// </para>
/// </remarks>
public sealed class ChaveAssinaturaProvider : IChaveAssinaturaProvider, IDisposable
{
    private const int TamanhoMinimoBits = 2048;
    private readonly RSA _rsa;

    public RsaSecurityKey ChavePrivada { get; }
    public RsaSecurityKey ChavePublica { get; }
    public string KeyId { get; }

    public ChaveAssinaturaProvider(IOptions<JwtOptions> opcoes, ILogger<ChaveAssinaturaProvider> logger)
    {
        var caminho = opcoes.Value.ChavePrivadaPath;

        if (!string.IsNullOrWhiteSpace(caminho))
        {
            // [TokenOptimization] - Chave carregada uma única vez (singleton) e reutilizada em todas as assinaturas.
            _rsa = RSA.Create();
            _rsa.ImportFromPem(File.ReadAllText(caminho));
            if (_rsa.KeySize < TamanhoMinimoBits)
                throw new InvalidOperationException($"A chave RSA de assinatura deve ter ao menos {TamanhoMinimoBits} bits.");
            GarantirChavePrivada(_rsa, caminho);
            logger.LogInformation("Chave de assinatura JWT carregada de {Caminho}", caminho);
        }
        else
        {
            _rsa = RSA.Create(TamanhoMinimoBits);
            logger.LogWarning("Jwt:ChavePrivadaPath não configurado: usando chave RSA temporária. " +
                              "Tokens emitidos deixam de valer quando o serviço reiniciar.");
        }

        var publica = new RsaSecurityKey(_rsa.ExportParameters(includePrivateParameters: false));
        KeyId = Base64UrlEncoder.Encode(publica.ComputeJwkThumbprint());
        publica.KeyId = KeyId;

        ChavePublica = publica;
        ChavePrivada = new RsaSecurityKey(_rsa) { KeyId = KeyId };
    }

    /// <summary>
    /// Um PEM só com a chave pública também é aceito pelo <c>ImportFromPem</c>; sem esta checagem o serviço
    /// subiria normalmente e todo login falharia na hora de assinar o token. Falha rápida na inicialização.
    /// </summary>
    private static void GarantirChavePrivada(RSA rsa, string caminho)
    {
        try
        {
            // [TokenOptimization] - Uma assinatura de 1 byte na inicialização basta para provar que a chave privada existe.
            rsa.SignData(new byte[] { 0 }, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1);
        }
        catch (CryptographicException ex)
        {
            throw new InvalidOperationException(
                $"O arquivo {caminho} (Jwt:ChavePrivadaPath) precisa conter a chave privada RSA, não apenas a pública.", ex);
        }
    }

    public void Dispose() => _rsa.Dispose();
}
