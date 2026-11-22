using System.Net;
using System.Net.Http.Json;
using System.Security.Cryptography;
using System.Text;
using IdentidadeApi.Contracts;
using IdentidadeApi.Repositories;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;

namespace IdentidadeApi.Tests;

/// <summary>
/// Sessão com refresh token em cookie httpOnly: rotação a cada uso, detecção de reuso (roubo de token),
/// expiração e logout. O token de acesso continua curto (15 min) e só o refresh fica no cookie.
/// </summary>
public class RefreshTokenTests : IClassFixture<IdentidadeApiFactory>
{
    private const string Cookie = "vinheria_refresh";
    private readonly WebApplicationFactory<Program> _factory;
    private readonly RefreshTokenRepositoryFake _repositorio = new();
    private readonly RelogioFalso _relogio = new();

    public RefreshTokenTests(IdentidadeApiFactory factory)
    {
        // [TokenOptimization] - Repositório e relógio próprios por teste, sobre a fixture já configurada.
        _factory = factory.WithWebHostBuilder(b => b.ConfigureServices(s =>
        {
            s.RemoveAll<IRefreshTokenRepository>();
            s.AddSingleton<IRefreshTokenRepository>(_repositorio);
            s.RemoveAll<TimeProvider>();
            s.AddSingleton<TimeProvider>(_relogio);
        }));
    }

    // Cookies gerenciados manualmente para inspecionar os atributos e simular cada cenário.
    private HttpClient Cliente() => _factory.CreateClient(new WebApplicationFactoryClientOptions
    {
        BaseAddress = new Uri("https://localhost"),
        HandleCookies = false,
    });

    [Fact]
    public async Task Login_DefineCookieDeRefresh_HttpOnly_Secure_SameSiteStrict_ERestritoAIdentidade()
    {
        var response = await Cliente().PostAsJsonAsync("/api/v1/identidade/login", new LoginRequest("admin@agnello.com", "admin123"));

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var setCookie = Assert.Single(response.Headers.GetValues("Set-Cookie"), c => c.StartsWith(Cookie + "=")).ToLowerInvariant();
        Assert.Contains("httponly", setCookie);
        Assert.Contains("secure", setCookie);
        Assert.Contains("samesite=strict", setCookie);
        Assert.Contains("path=/api/v1/identidade", setCookie);
        Assert.Contains("expires=", setCookie);
    }

    [Fact]
    public async Task Login_NaoExpoeORefreshTokenNoCorpo_EBancoGuardaSoOHash()
    {
        var response = await Cliente().PostAsJsonAsync("/api/v1/identidade/login", new LoginRequest("admin@agnello.com", "admin123"));
        var refresh = ValorDoCookie(response);

        var corpo = await response.Content.ReadAsStringAsync();
        Assert.DoesNotContain(refresh, corpo);
        var salvo = Assert.Single(_repositorio.Tokens);
        Assert.NotEqual(refresh, salvo.TokenHash);
        Assert.Equal(Sha256(refresh), salvo.TokenHash);
        Assert.Equal(1, salvo.UsuarioId);
    }

    [Fact]
    public async Task Refresh_ComCookieValido_EmiteNovoAccessToken_ERotacionaORefresh()
    {
        var refresh = ValorDoCookie(await LoginAsync());

        var response = await RefreshAsync(refresh);

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var corpo = await response.Content.ReadFromJsonAsync<LoginResponse>();
        Assert.False(string.IsNullOrWhiteSpace(corpo!.AccessToken));
        Assert.Equal("Bearer", corpo.TokenType);
        var novo = ValorDoCookie(response);
        Assert.NotEqual(refresh, novo);
        // O token antigo fica revogado e aponta para o que o substituiu.
        var antigo = _repositorio.Tokens.Single(t => t.TokenHash == Sha256(refresh));
        Assert.NotNull(antigo.RevogadoEm);
        Assert.Equal(Sha256(novo), antigo.SubstituidoPorHash);
    }

    [Theory]
    [InlineData(null)]
    [InlineData("token-que-nunca-existiu")]
    public async Task Refresh_SemCookieOuComTokenDesconhecido_Retorna401(string? valor)
    {
        var response = await RefreshAsync(valor);

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Refresh_ReusoDeTokenJaUsado_Retorna401_ERevogaTodasAsSessoesDoUsuario()
    {
        var original = ValorDoCookie(await LoginAsync());
        var rotacionado = ValorDoCookie(await RefreshAsync(original));
        // Segunda sessão do mesmo usuário (ex.: outro navegador), que também deve cair.
        var outraSessao = ValorDoCookie(await LoginAsync());

        // Um atacante (ou o cliente legítimo) apresenta o token antigo de novo: sinal de roubo.
        var reuso = await RefreshAsync(original);
        Assert.Equal(HttpStatusCode.Unauthorized, reuso.StatusCode);

        // Por segurança, até os tokens mais novos deixam de valer: o usuário precisa logar de novo.
        Assert.Equal(HttpStatusCode.Unauthorized, (await RefreshAsync(rotacionado)).StatusCode);
        Assert.Equal(HttpStatusCode.Unauthorized, (await RefreshAsync(outraSessao)).StatusCode);
        Assert.Equal(3, _repositorio.Tokens.Count);
        Assert.All(_repositorio.Tokens, t => Assert.NotNull(t.RevogadoEm));
    }

    [Fact]
    public async Task Refresh_TokenRevogadoQueJaExpirou_Retorna401_SemDerrubarAsOutrasSessoes()
    {
        // Sessão antiga: rotacionada (token original revogado) e depois abandonada.
        var antigo = ValorDoCookie(await LoginAsync());
        await RefreshAsync(antigo);

        _relogio.Avancar(TimeSpan.FromDays(6));
        var atual = ValorDoCookie(await LoginAsync());
        _relogio.Avancar(TimeSpan.FromDays(2)); // o token antigo já expirou; o atual ainda vale

        // Um token expirado não serve para nada: reapresentá-lo não pode derrubar as sessões válidas.
        Assert.Equal(HttpStatusCode.Unauthorized, (await RefreshAsync(antigo)).StatusCode);
        Assert.Equal(HttpStatusCode.OK, (await RefreshAsync(atual)).StatusCode);
    }

    [Fact]
    public async Task Refresh_TokenExpirado_Retorna401()
    {
        var refresh = ValorDoCookie(await LoginAsync());

        _relogio.Avancar(TimeSpan.FromDays(8)); // validade padrão: 7 dias
        var response = await RefreshAsync(refresh);

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Logout_RevogaORefresh_EApagaOCookie()
    {
        var refresh = ValorDoCookie(await LoginAsync());

        var requisicao = new HttpRequestMessage(HttpMethod.Post, "/api/v1/identidade/logout");
        requisicao.Headers.Add("Cookie", $"{Cookie}={refresh}");
        var logout = await Cliente().SendAsync(requisicao);

        Assert.Equal(HttpStatusCode.NoContent, logout.StatusCode);
        var apagado = Assert.Single(logout.Headers.GetValues("Set-Cookie"), c => c.StartsWith(Cookie + "="));
        Assert.Contains("expires=thu, 01 jan 1970", apagado.ToLowerInvariant());
        Assert.Equal(HttpStatusCode.Unauthorized, (await RefreshAsync(refresh)).StatusCode);
    }

    // ---------- auxiliares ----------

    private Task<HttpResponseMessage> LoginAsync() =>
        Cliente().PostAsJsonAsync("/api/v1/identidade/login", new LoginRequest("admin@agnello.com", "admin123"));

    private Task<HttpResponseMessage> RefreshAsync(string? valor)
    {
        var requisicao = new HttpRequestMessage(HttpMethod.Post, "/api/v1/identidade/refresh");
        if (valor is not null) requisicao.Headers.Add("Cookie", $"{Cookie}={valor}");
        return Cliente().SendAsync(requisicao);
    }

    private static string ValorDoCookie(HttpResponseMessage response)
    {
        var setCookie = response.Headers.GetValues("Set-Cookie").Single(c => c.StartsWith(Cookie + "="));
        return setCookie.Split(';')[0][(Cookie.Length + 1)..];
    }

    private static string Sha256(string valor) =>
        Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(valor))).ToLowerInvariant();
}
