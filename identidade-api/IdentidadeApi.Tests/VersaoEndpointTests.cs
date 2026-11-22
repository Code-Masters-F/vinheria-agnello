using System.Net;
using System.Text.Json;
using Microsoft.AspNetCore.Hosting;

namespace IdentidadeApi.Tests;

/// <summary>
/// /versao informa qual release está em execução; a verificação pós-deploy do pipeline usa essa rota
/// para confirmar que o serviço reciclou para a versão recém-publicada.
/// </summary>
public class VersaoEndpointTests : IClassFixture<IdentidadeApiFactory>
{
    private readonly IdentidadeApiFactory _factory;

    public VersaoEndpointTests(IdentidadeApiFactory factory) => _factory = factory;

    [Fact]
    public async Task Versao_InformaAVersaoPublicada()
    {
        // [TokenOptimization] - Sobrescreve só a variável APP_VERSAO na fixture já configurada.
        var client = _factory.WithWebHostBuilder(b => b.UseSetting("APP_VERSAO", "42")).CreateClient();

        var response = await client.GetAsync("/versao");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        using var json = JsonDocument.Parse(await response.Content.ReadAsStringAsync());
        Assert.Equal("identidade", json.RootElement.GetProperty("servico").GetString());
        Assert.Equal("42", json.RootElement.GetProperty("versao").GetString());
    }

    [Fact]
    public async Task Versao_SemDeploy_InformaDev()
    {
        var response = await _factory.CreateClient().GetAsync("/versao");

        using var json = JsonDocument.Parse(await response.Content.ReadAsStringAsync());
        Assert.Equal("dev", json.RootElement.GetProperty("versao").GetString());
    }
}
