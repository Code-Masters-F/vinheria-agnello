using System.Net;
using System.Net.Http.Json;
using System.Security.Cryptography;
using System.Text.Json;
using IdentidadeApi.Contracts;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Tokens;

namespace IdentidadeApi.Tests;

/// <summary>
/// Simula o que o Catálogo fará: ler o discovery/JWKS da Identidade e validar o token só com a chave pública.
/// </summary>
public class TokenEJwksTests : IClassFixture<IdentidadeApiFactory>
{
    private readonly IdentidadeApiFactory _factory;
    private readonly HttpClient _client;

    public TokenEJwksTests(IdentidadeApiFactory factory)
    {
        _factory = factory;
        // [TokenOptimization] - Um único HttpClient por classe de teste, reaproveitado entre os casos.
        _client = factory.CreateClient();
    }

    [Fact]
    public async Task OpenIdConfiguration_InformaIssuerEJwksUri()
    {
        using var doc = await GetJsonAsync("/.well-known/openid-configuration");
        var raiz = doc.RootElement;

        Assert.Equal(IdentidadeApiFactory.Issuer, raiz.GetProperty("issuer").GetString());
        Assert.Equal($"{IdentidadeApiFactory.Issuer}/.well-known/jwks.json", raiz.GetProperty("jwks_uri").GetString());
        Assert.Contains("RS256", raiz.GetProperty("id_token_signing_alg_values_supported").EnumerateArray().Select(e => e.GetString()));
    }

    [Fact]
    public async Task OpenIdConfiguration_MantemOIssuerExatamenteComoConfigurado()
    {
        // O "iss" dos tokens usa o Issuer sem alterações; o discovery precisa informar o mesmo valor,
        // senão o Catálogo recusaria todos os tokens. Só o jwks_uri é normalizado.
        const string issuerComBarra = IdentidadeApiFactory.Issuer + "/";
        var cliente = _factory.WithWebHostBuilder(b => b.ConfigureAppConfiguration((_, config) =>
            config.AddInMemoryCollection(new Dictionary<string, string?> { ["Jwt:Issuer"] = issuerComBarra }))).CreateClient();

        using var doc = await JsonDocument.ParseAsync(await cliente.GetStreamAsync("/.well-known/openid-configuration"));

        Assert.Equal(issuerComBarra, doc.RootElement.GetProperty("issuer").GetString());
        Assert.Equal($"{IdentidadeApiFactory.Issuer}/.well-known/jwks.json", doc.RootElement.GetProperty("jwks_uri").GetString());
    }

    [Fact]
    public async Task Jwks_PublicaSomenteAChavePublicaRsa()
    {
        using var doc = await GetJsonAsync("/.well-known/jwks.json");
        var chave = Assert.Single(doc.RootElement.GetProperty("keys").EnumerateArray());

        Assert.Equal("RSA", chave.GetProperty("kty").GetString());
        Assert.Equal("RS256", chave.GetProperty("alg").GetString());
        Assert.Equal("sig", chave.GetProperty("use").GetString());
        Assert.False(string.IsNullOrWhiteSpace(chave.GetProperty("kid").GetString()));
        Assert.False(string.IsNullOrWhiteSpace(chave.GetProperty("n").GetString()));
        Assert.False(string.IsNullOrWhiteSpace(chave.GetProperty("e").GetString()));
        // Nenhum parâmetro privado (d, p, q, dp, dq, qi) pode vazar no JWKS.
        foreach (var privado in new[] { "d", "p", "q", "dp", "dq", "qi" })
            Assert.False(chave.TryGetProperty(privado, out _), $"JWKS expõe o parâmetro privado '{privado}'");
    }

    [Fact]
    public async Task TokenEmitido_EhValidadoComAChavePublicaDoJwks_EContemAsClaims()
    {
        var token = await LoginAsync("admin@agnello.com", "admin123");
        var chavePublica = await ObterChavePublicaDoJwksAsync();

        var resultado = await new JsonWebTokenHandler().ValidateTokenAsync(token, new TokenValidationParameters
        {
            ValidIssuer = IdentidadeApiFactory.Issuer,
            ValidAudience = IdentidadeApiFactory.Audience,
            IssuerSigningKey = chavePublica,
            ValidAlgorithms = new[] { SecurityAlgorithms.RsaSha256 },
        });

        Assert.True(resultado.IsValid, resultado.Exception?.Message);
        var jwt = (JsonWebToken)resultado.SecurityToken;
        Assert.Equal(chavePublica.KeyId, jwt.Kid);
        Assert.Equal("1", jwt.Subject);
        Assert.Equal("admin@agnello.com", jwt.GetClaim("email").Value);
        Assert.Equal("Admin", jwt.GetClaim("role").Value);
        Assert.Equal("1", jwt.GetClaim("vinheria_id").Value);
        Assert.False(string.IsNullOrWhiteSpace(jwt.Id)); // jti
        Assert.Equal(TimeSpan.FromMinutes(15), jwt.ValidTo - jwt.IssuedAt);
    }

    [Fact]
    public async Task TokenDeCliente_TemPerfilCliente()
    {
        var token = await LoginAsync("cliente@agnello.com", "cliente123");

        var jwt = new JsonWebToken(token);
        Assert.Equal("Cliente", jwt.GetClaim("role").Value);
        Assert.Equal("2", jwt.Subject);
    }

    [Fact]
    public async Task TokenAdulterado_EhRejeitado()
    {
        var token = await LoginAsync("cliente@agnello.com", "cliente123");
        var chavePublica = await ObterChavePublicaDoJwksAsync();

        // Troca o payload por um que se declara Admin, mantendo a assinatura original.
        var partes = token.Split('.');
        var payload = JsonSerializer.Deserialize<Dictionary<string, object>>(Base64UrlEncoder.Decode(partes[1]))!;
        payload["role"] = "Admin";
        var adulterado = $"{partes[0]}.{Base64UrlEncoder.Encode(JsonSerializer.Serialize(payload))}.{partes[2]}";

        var resultado = await new JsonWebTokenHandler().ValidateTokenAsync(adulterado, new TokenValidationParameters
        {
            ValidIssuer = IdentidadeApiFactory.Issuer,
            ValidAudience = IdentidadeApiFactory.Audience,
            IssuerSigningKey = chavePublica,
        });

        Assert.False(resultado.IsValid);
    }

    // ---------- auxiliares ----------

    private async Task<JsonDocument> GetJsonAsync(string rota)
    {
        var response = await _client.GetAsync(rota);
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        // [TokenOptimization] - Lê o stream direto para o JsonDocument, sem string intermediária.
        return await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
    }

    private async Task<string> LoginAsync(string email, string senha)
    {
        var response = await _client.PostAsJsonAsync("/api/v1/identidade/login", new LoginRequest(email, senha));
        response.EnsureSuccessStatusCode();
        return (await response.Content.ReadFromJsonAsync<LoginResponse>())!.AccessToken;
    }

    private async Task<RsaSecurityKey> ObterChavePublicaDoJwksAsync()
    {
        using var doc = await GetJsonAsync("/.well-known/jwks.json");
        var chave = doc.RootElement.GetProperty("keys")[0];
        var parametros = new RSAParameters
        {
            Modulus = Base64UrlEncoder.DecodeBytes(chave.GetProperty("n").GetString()),
            Exponent = Base64UrlEncoder.DecodeBytes(chave.GetProperty("e").GetString()),
        };
        return new RsaSecurityKey(parametros) { KeyId = chave.GetProperty("kid").GetString() };
    }
}
