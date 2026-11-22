using System.Security.Cryptography;
using IdentidadeApi.Security;
using Microsoft.Extensions.Logging.Abstractions;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace IdentidadeApi.Tests;

/// <summary>
/// A chave de assinatura é carregada de um PEM montado no container. Um arquivo errado (só com a chave
/// pública) precisa impedir o serviço de subir, em vez de deixar todo login falhar depois.
/// </summary>
public class ChaveAssinaturaProviderTests
{
    [Fact]
    public void PemComChavePrivada_CarregaAsChavesDeAssinatura()
    {
        using var rsa = RSA.Create(2048);
        var arquivo = Salvar(rsa.ExportPkcs8PrivateKeyPem());
        try
        {
            using var provider = Criar(arquivo);

            Assert.False(string.IsNullOrWhiteSpace(provider.KeyId));
            Assert.Equal(PrivateKeyStatus.Exists, provider.ChavePrivada.PrivateKeyStatus);
        }
        finally
        {
            File.Delete(arquivo);
        }
    }

    [Fact]
    public void PemSomenteComChavePublica_ImpedeAInicializacao()
    {
        using var rsa = RSA.Create(2048);
        var arquivo = Salvar(rsa.ExportSubjectPublicKeyInfoPem());
        try
        {
            var erro = Assert.Throws<InvalidOperationException>(() => Criar(arquivo));
            Assert.Contains("chave privada", erro.Message);
        }
        finally
        {
            File.Delete(arquivo);
        }
    }

    private static ChaveAssinaturaProvider Criar(string caminho) =>
        // [TokenOptimization] - Opções e logger mínimos, sem subir o host da API.
        new(Options.Create(new JwtOptions { ChavePrivadaPath = caminho }), NullLogger<ChaveAssinaturaProvider>.Instance);

    private static string Salvar(string pem)
    {
        var arquivo = Path.GetTempFileName();
        File.WriteAllText(arquivo, pem);
        return arquivo;
    }
}
