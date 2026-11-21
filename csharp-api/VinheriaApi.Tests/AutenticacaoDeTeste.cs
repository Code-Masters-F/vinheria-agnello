using System;
using System.Collections.Generic;
using System.Security.Cryptography;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Protocols;
using Microsoft.IdentityModel.Protocols.OpenIdConnect;
using Microsoft.IdentityModel.Tokens;

namespace VinheriaApi.Tests
{
    /// <summary>
    /// Emite tokens no mesmo formato da Identidade (RS256, claims role/vinheria_id) com uma chave de teste.
    /// </summary>
    public static class TokensDeTeste
    {
        public const string Issuer = "https://identidade.teste";
        public const string Audience = "vinheria";

        private static readonly RsaSecurityKey ChaveDaIdentidade = new(RSA.Create(2048)) { KeyId = "chave-teste" };
        private static readonly RsaSecurityKey ChaveDeAtacante = new(RSA.Create(2048)) { KeyId = "chave-teste" };
        private static readonly JsonWebTokenHandler Handler = new();

        public static string Admin(long vinheriaId = 1) => Gerar("Admin", vinheriaId);
        public static string Cliente(long vinheriaId = 1) => Gerar("Cliente", vinheriaId);

        public static string Expirado() =>
            Gerar("Admin", 1, emitidoEm: DateTime.UtcNow.AddHours(-2), validade: TimeSpan.FromMinutes(15));

        /// <summary>Token com o mesmo kid, mas assinado com outra chave (falsificação).</summary>
        public static string AssinadoPorOutraChave() => Gerar("Admin", 1, chave: ChaveDeAtacante);

        public static string ComAudience(string audience) => Gerar("Admin", 1, audience: audience);
        public static string ComIssuer(string issuer) => Gerar("Admin", 1, issuer: issuer);

        public static string Gerar(string perfil, long vinheriaId, DateTime? emitidoEm = null, TimeSpan? validade = null,
            RsaSecurityKey? chave = null, string? audience = null, string? issuer = null)
        {
            var inicio = emitidoEm ?? DateTime.UtcNow;
            // [TokenOptimization] - Handler e chaves estáticos reutilizados por todos os testes.
            return Handler.CreateToken(new SecurityTokenDescriptor
            {
                Issuer = issuer ?? Issuer,
                Audience = audience ?? Audience,
                IssuedAt = inicio,
                NotBefore = inicio,
                Expires = inicio.Add(validade ?? TimeSpan.FromMinutes(15)),
                SigningCredentials = new SigningCredentials(chave ?? ChaveDaIdentidade, SecurityAlgorithms.RsaSha256),
                Claims = new Dictionary<string, object>
                {
                    ["sub"] = "1",
                    ["role"] = perfil,
                    ["vinheria_id"] = vinheriaId,
                },
            });
        }

        /// <summary>
        /// Configura a API para os testes: Authority fictícia e chave de assinatura fixa (sem buscar o JWKS pela rede).
        /// </summary>
        public static void ConfigurarParaTestes(this IWebHostBuilder builder)
        {
            // [TokenOptimization] - Configuração aplicada uma vez por fixture.
            builder.UseSetting("ConnectionStrings:DefaultConnection", "Host=teste;Database=teste");
            builder.UseSetting("Jwt:Authority", Issuer);

            builder.ConfigureServices(services =>
            {
                services.PostConfigure<JwtBearerOptions>(JwtBearerDefaults.AuthenticationScheme, opcoes =>
                {
                    var configuracao = new OpenIdConnectConfiguration { Issuer = Issuer };
                    configuracao.SigningKeys.Add(ChaveDaIdentidade);
                    opcoes.Configuration = configuracao;
                    opcoes.ConfigurationManager = new StaticConfigurationManager<OpenIdConnectConfiguration>(configuracao);
                });
            });
        }
    }
}
