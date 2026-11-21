using System;
using System.IO;
using System.Net.Http;
using System.Net.Security;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using VinheriaApi.Security;
using Xunit;

namespace VinheriaApi.Tests
{
    /// <summary>
    /// O Catálogo busca o JWKS da Identidade por HTTPS e só deve confiar em certificados emitidos
    /// pela CA interna da Vinheria, para o nome correto e dentro da validade.
    /// </summary>
    public class ValidacaoTlsTests
    {
        private const string Host = "identidade.vinheria.local";
        private static readonly X509Certificate2 CaVinheria = CriarCa("Vinheria Agnello CA Interna");
        private static readonly X509Certificate2 CaDesconhecida = CriarCa("CA Desconhecida");

        // O sistema operacional não conhece a CA interna, então sempre reporta erro de cadeia;
        // a validação customizada refaz a cadeia usando somente a CA da Vinheria.
        private const SslPolicyErrors ErroDeCadeiaDoSistema = SslPolicyErrors.RemoteCertificateChainErrors;

        [Fact]
        public void CertificadoEmitidoPelaCaInterna_EhAceito()
        {
            var certificado = CriarCertificadoDeServidor(CaVinheria, Host);

            Assert.True(ValidacaoTls.CertificadoConfiavel(certificado, ErroDeCadeiaDoSistema, CaVinheria));
        }

        [Fact]
        public void CertificadoEmitidoPorOutraCa_EhRecusado()
        {
            var certificado = CriarCertificadoDeServidor(CaDesconhecida, Host);

            Assert.False(ValidacaoTls.CertificadoConfiavel(certificado, ErroDeCadeiaDoSistema, CaVinheria));
        }

        [Fact]
        public void CertificadoAutoAssinado_EhRecusado()
        {
            var autoAssinado = CriarCa(Host);

            Assert.False(ValidacaoTls.CertificadoConfiavel(autoAssinado, ErroDeCadeiaDoSistema, CaVinheria));
        }

        [Fact]
        public void NomeDoHostDiferenteDoCertificado_EhRecusado()
        {
            var certificado = CriarCertificadoDeServidor(CaVinheria, "outro-servico.vinheria.local");
            var erros = ErroDeCadeiaDoSistema | SslPolicyErrors.RemoteCertificateNameMismatch;

            Assert.False(ValidacaoTls.CertificadoConfiavel(certificado, erros, CaVinheria));
        }

        [Fact]
        public void CertificadoExpirado_EhRecusado()
        {
            var expirado = CriarCertificadoDeServidor(CaVinheria, Host,
                inicio: DateTimeOffset.UtcNow.AddDays(-30), fim: DateTimeOffset.UtcNow.AddDays(-1));

            Assert.False(ValidacaoTls.CertificadoConfiavel(expirado, ErroDeCadeiaDoSistema, CaVinheria));
        }

        [Fact]
        public void SemCertificado_EhRecusado()
        {
            Assert.False(ValidacaoTls.CertificadoConfiavel(null, SslPolicyErrors.RemoteCertificateNotAvailable, CaVinheria));
        }

        [Fact]
        public void ComJwtCaCertPath_OCanalDoJwksUsaAValidacaoComACaInterna()
        {
            var arquivoCa = Path.GetTempFileName();
            try
            {
                File.WriteAllText(arquivoCa, CaVinheria.ExportCertificatePem());
                using var factory = new WebApplicationFactory<Program>().WithWebHostBuilder(builder =>
                {
                    builder.ConfigurarParaTestes();
                    builder.UseSetting("Jwt:CaCertPath", arquivoCa);
                });

                // [TokenOptimization] - Resolve as opções uma única vez, sem subir requisições HTTP.
                var opcoes = factory.Services.GetRequiredService<IOptionsMonitor<JwtBearerOptions>>()
                    .Get(JwtBearerDefaults.AuthenticationScheme);

                var handler = Assert.IsType<HttpClientHandler>(opcoes.BackchannelHttpHandler);
                Assert.NotNull(handler.ServerCertificateCustomValidationCallback);
            }
            finally
            {
                File.Delete(arquivoCa);
            }
        }

        // ---------- geração de certificados para os testes ----------

        private static X509Certificate2 CriarCa(string nome)
        {
            using var chave = RSA.Create(2048);
            var requisicao = new CertificateRequest($"CN={nome}", chave, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1);
            requisicao.CertificateExtensions.Add(new X509BasicConstraintsExtension(true, false, 0, true));
            requisicao.CertificateExtensions.Add(new X509KeyUsageExtension(X509KeyUsageFlags.KeyCertSign | X509KeyUsageFlags.CrlSign, true));
            requisicao.CertificateExtensions.Add(new X509SubjectKeyIdentifierExtension(requisicao.PublicKey, false));
            return requisicao.CreateSelfSigned(DateTimeOffset.UtcNow.AddDays(-60), DateTimeOffset.UtcNow.AddYears(2));
        }

        private static X509Certificate2 CriarCertificadoDeServidor(X509Certificate2 ca, string host,
            DateTimeOffset? inicio = null, DateTimeOffset? fim = null)
        {
            using var chave = RSA.Create(2048);
            var requisicao = new CertificateRequest($"CN={host}", chave, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1);
            requisicao.CertificateExtensions.Add(new X509BasicConstraintsExtension(false, false, 0, false));
            requisicao.CertificateExtensions.Add(new X509EnhancedKeyUsageExtension(
                new OidCollection { new Oid("1.3.6.1.5.5.7.3.1") }, false)); // serverAuth
            var san = new SubjectAlternativeNameBuilder();
            san.AddDnsName(host);
            requisicao.CertificateExtensions.Add(san.Build());

            return requisicao.Create(ca, inicio ?? DateTimeOffset.UtcNow.AddDays(-1), fim ?? DateTimeOffset.UtcNow.AddDays(90),
                RandomNumberGenerator.GetBytes(16));
        }
    }
}
