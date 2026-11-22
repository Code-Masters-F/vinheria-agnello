using System.Net;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using IdentidadeApi.Contracts;

namespace IdentidadeApi.Tests;

public class LoginEndpointTests : IClassFixture<IdentidadeApiFactory>
{
    private const string RotaLogin = "/api/v1/identidade/login";
    private readonly HttpClient _client;

    public LoginEndpointTests(IdentidadeApiFactory factory)
    {
        // [TokenOptimization] - Um único HttpClient por classe de teste, reaproveitado entre os casos.
        _client = factory.CreateClient();
    }

    [Fact]
    public async Task Login_ComCredenciaisValidas_Retorna200ComTokenBearer()
    {
        var response = await _client.PostAsJsonAsync(RotaLogin, new LoginRequest("admin@agnello.com", "admin123"));

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var corpo = await response.Content.ReadFromJsonAsync<LoginResponse>();
        Assert.NotNull(corpo);
        Assert.False(string.IsNullOrWhiteSpace(corpo!.AccessToken));
        Assert.Equal("Bearer", corpo.TokenType);
        Assert.Equal(15 * 60, corpo.ExpiresIn);
    }

    [Fact]
    public async Task Login_EmailSemDiferenciarMaiusculas_Retorna200()
    {
        var response = await _client.PostAsJsonAsync(RotaLogin, new LoginRequest("  ADMIN@Agnello.com ", "admin123"));

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }

    [Theory]
    [InlineData("admin@agnello.com", "senha-errada")]     // senha incorreta
    [InlineData("naoexiste@agnello.com", "admin123")]    // usuário inexistente
    [InlineData("inativo@agnello.com", "inativo123")]    // usuário desativado
    [InlineData("corrompido@agnello.com", "qualquer")]   // hash inválido no banco não pode virar erro 500
    public async Task Login_ComCredenciaisInvalidas_Retorna401SemRevelarMotivo(string email, string senha)
    {
        var response = await _client.PostAsJsonAsync(RotaLogin, new LoginRequest(email, senha));

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
        using var problema = JsonDocument.Parse(await response.Content.ReadAsStringAsync());
        // A mesma mensagem para todos os casos evita enumeração de usuários.
        Assert.Equal("Credenciais inválidas", problema.RootElement.GetProperty("title").GetString());
        Assert.False(problema.RootElement.TryGetProperty("accessToken", out _));
    }

    [Theory]
    [InlineData("{}")]
    [InlineData("{\"email\":\"\",\"senha\":\"admin123\"}")]
    [InlineData("{\"email\":\"admin@agnello.com\",\"senha\":\"\"}")]
    [InlineData("{\"email\":\"   \",\"senha\":\"   \"}")]
    public async Task Login_SemEmailOuSenha_Retorna400(string json)
    {
        var response = await _client.PostAsync(RotaLogin, new StringContent(json, Encoding.UTF8, "application/json"));

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task Health_Retorna200()
    {
        var response = await _client.GetAsync("/health");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
    }
}
