using System.Net.Http;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;

namespace VinheriaApi.Security
{
    /// <summary>
    /// Validação TLS das chamadas do Catálogo para a Identidade (busca do discovery/JWKS).
    /// </summary>
    /// <remarks>
    /// Os certificados dos serviços são emitidos pela CA interna da Vinheria, que o sistema operacional
    /// não conhece (por isso ele sempre reporta <see cref="SslPolicyErrors.RemoteCertificateChainErrors"/>).
    /// Em vez de desligar a validação, a cadeia é refeita confiando <b>somente</b> nessa CA
    /// (<see cref="X509ChainTrustMode.CustomRootTrust"/>): certificados de qualquer outra origem,
    /// auto-assinados, expirados ou emitidos para outro nome são recusados.
    /// </remarks>
    public static class ValidacaoTls
    {
        private const SslPolicyErrors ErrosQueInvalidamSempre =
            SslPolicyErrors.RemoteCertificateNotAvailable | SslPolicyErrors.RemoteCertificateNameMismatch;

        public static bool CertificadoConfiavel(X509Certificate2? certificado, SslPolicyErrors erros, X509Certificate2 ca)
        {
            // Sem certificado ou com nome diferente do host chamado: recusa, independentemente da cadeia.
            if (certificado is null || (erros & ErrosQueInvalidamSempre) != 0) return false;

            // [TokenOptimization] - Cadeia construída só com a CA interna, sem consultar repositórios do sistema nem a rede.
            using var cadeia = new X509Chain();
            cadeia.ChainPolicy.TrustMode = X509ChainTrustMode.CustomRootTrust;
            cadeia.ChainPolicy.CustomTrustStore.Add(ca);
            // CA privada e ambiente de laboratório: não há lista de revogação publicada.
            cadeia.ChainPolicy.RevocationMode = X509RevocationMode.NoCheck;

            return cadeia.Build(certificado);
        }

        /// <summary>Handler HTTP que só aceita servidores com certificado emitido pela <paramref name="ca"/>.</summary>
        public static HttpMessageHandler CriarHandlerConfiandoNaCa(X509Certificate2 ca)
        {
            return new HttpClientHandler
            {
                ServerCertificateCustomValidationCallback = (_, certificado, _, erros) =>
                    CertificadoConfiavel(certificado, erros, ca),
            };
        }
    }
}
