using System.Linq;
using System.Net;
using System.Text.Json;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Moq;
using VinheriaApi.Repositories;
using Xunit;

namespace VinheriaApi.Tests
{
    /// <summary>
    /// /versao informa qual release está em execução (usada na verificação pós-deploy do pipeline).
    /// </summary>
    public class VersaoEndpointTests : IClassFixture<WebApplicationFactory<Program>>
    {
        private readonly WebApplicationFactory<Program> _factory;

        public VersaoEndpointTests(WebApplicationFactory<Program> factory) => _factory = factory;

        private WebApplicationFactory<Program> Criar(string? versao) => _factory.WithWebHostBuilder(builder =>
        {
            builder.ConfigurarParaTestes();
            if (versao is not null) builder.UseSetting("APP_VERSAO", versao);
            builder.ConfigureServices(services =>
            {
                var descriptor = services.SingleOrDefault(d => d.ServiceType == typeof(IProdutoRepository));
                if (descriptor != null) services.Remove(descriptor);
                services.AddSingleton(new Mock<IProdutoRepository>().Object);
            });
        });

        [Fact]
        public async Task Versao_InformaAVersaoPublicada()
        {
            // [TokenOptimization] - Uma requisição simples, sem autenticação nem banco.
            var response = await Criar("42").CreateClient().GetAsync("/versao");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
            using var json = JsonDocument.Parse(await response.Content.ReadAsStringAsync());
            Assert.Equal("catalogo", json.RootElement.GetProperty("servico").GetString());
            Assert.Equal("42", json.RootElement.GetProperty("versao").GetString());
        }

        [Fact]
        public async Task Versao_SemDeploy_InformaDev()
        {
            var response = await Criar(null).CreateClient().GetAsync("/versao");

            using var json = JsonDocument.Parse(await response.Content.ReadAsStringAsync());
            Assert.Equal("dev", json.RootElement.GetProperty("versao").GetString());
        }
    }
}
