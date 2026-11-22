using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Moq;
using VinheriaApi.Models;
using VinheriaApi.Repositories;
using Xunit;

namespace VinheriaApi.Tests
{
    /// <summary>
    /// Regras de acesso do Catálogo (RBAC da arquitetura de microsserviços):
    /// leitura pública; criação, edição e exclusão só para Admin da própria vinheria.
    /// </summary>
    public class AutorizacaoEndpointTests : IClassFixture<WebApplicationFactory<Program>>
    {
        private const string Rota = "/api/v1/catalogo/produtos";
        private readonly WebApplicationFactory<Program> _factory;
        private readonly Mock<IProdutoRepository> _repo = new();

        public AutorizacaoEndpointTests(WebApplicationFactory<Program> factory)
        {
            // Produto 1 pertence à vinheria 1; produto 2 pertence à vinheria 2.
            _repo.Setup(r => r.GetAllAsync()).ReturnsAsync(new[] { new Produto { Id = 1, VinheriaId = 1, Nome = "Tinto" } });
            _repo.Setup(r => r.GetByIdAsync(1)).ReturnsAsync(new Produto { Id = 1, VinheriaId = 1, Nome = "Tinto" });
            _repo.Setup(r => r.GetByIdAsync(2)).ReturnsAsync(new Produto { Id = 2, VinheriaId = 2, Nome = "Branco" });
            _repo.Setup(r => r.CreateAsync(It.IsAny<Produto>())).ReturnsAsync(10);
            _repo.Setup(r => r.UpdateAsync(It.IsAny<Produto>())).ReturnsAsync(true);
            _repo.Setup(r => r.DeleteAsync(It.IsAny<long>())).ReturnsAsync(true);

            _factory = factory.WithWebHostBuilder(builder =>
            {
                builder.ConfigurarParaTestes();
                builder.ConfigureServices(services =>
                {
                    var descriptor = services.SingleOrDefault(d => d.ServiceType == typeof(IProdutoRepository));
                    if (descriptor != null) services.Remove(descriptor);
                    services.AddSingleton(_repo.Object);
                });
            });
        }

        // ---------- leitura pública ----------

        [Theory]
        [InlineData(Rota)]
        [InlineData(Rota + "/1")]
        public async Task Leitura_SemToken_Retorna200(string url)
        {
            var response = await _factory.CreateClient().GetAsync(url);

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        }

        // ---------- escrita: autenticação e perfil ----------

        public static TheoryData<string, string> OperacoesDeEscrita => new()
        {
            { "POST", Rota },
            { "PUT", Rota + "/1" },
            { "DELETE", Rota + "/1" },
            { "POST", "/api/produtos" },      // rota legada também protegida
            { "PUT", "/api/produtos/1" },
            { "DELETE", "/api/produtos/1" },
        };

        [Theory]
        [MemberData(nameof(OperacoesDeEscrita))]
        public async Task Escrita_SemToken_Retorna401(string metodo, string url)
        {
            var response = await EnviarAsync(metodo, url, token: null);

            Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
            NenhumaEscritaNoBanco();
        }

        [Theory]
        [MemberData(nameof(OperacoesDeEscrita))]
        public async Task Escrita_ComTokenDeCliente_Retorna403(string metodo, string url)
        {
            var response = await EnviarAsync(metodo, url, TokensDeTeste.Cliente());

            Assert.Equal(HttpStatusCode.Forbidden, response.StatusCode);
            NenhumaEscritaNoBanco();
        }

        [Fact]
        public async Task Criar_ComTokenDeAdmin_Retorna201_EUsaAVinheriaDoToken()
        {
            Produto? gravado = null;
            _repo.Setup(r => r.CreateAsync(It.IsAny<Produto>())).Callback<Produto>(p => gravado = p).ReturnsAsync(10);

            // O corpo tenta gravar na vinheria 99; o token é da vinheria 7.
            var response = await EnviarAsync("POST", Rota, TokensDeTeste.Admin(vinheriaId: 7),
                new Produto { Nome = "Novo", VinheriaId = 99, Preco = 50 });

            Assert.Equal(HttpStatusCode.Created, response.StatusCode);
            Assert.Equal(7, gravado!.VinheriaId);
            Assert.Equal($"{Rota}/10", response.Headers.Location!.OriginalString);
        }

        [Fact]
        public async Task Atualizar_ComTokenDeAdmin_Retorna204()
        {
            var response = await EnviarAsync("PUT", Rota + "/1", TokensDeTeste.Admin(1), new Produto { Id = 1, Nome = "Editado" });

            Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
            _repo.Verify(r => r.UpdateAsync(It.Is<Produto>(p => p.Id == 1 && p.VinheriaId == 1)), Times.Once);
        }

        [Fact]
        public async Task Excluir_ComTokenDeAdmin_Retorna204()
        {
            var response = await EnviarAsync("DELETE", Rota + "/1", TokensDeTeste.Admin(1));

            Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
            _repo.Verify(r => r.DeleteAsync(1), Times.Once);
        }

        // ---------- isolamento entre vinherias (multi-tenant) ----------

        [Theory]
        [InlineData("PUT")]
        [InlineData("DELETE")]
        public async Task AdminDeOutraVinheria_NaoAlteraProduto_Retorna403(string metodo)
        {
            // Admin da vinheria 1 tentando alterar o produto 2, que é da vinheria 2.
            var response = await EnviarAsync(metodo, Rota + "/2", TokensDeTeste.Admin(1), new Produto { Id = 2, Nome = "Invasão" });

            Assert.Equal(HttpStatusCode.Forbidden, response.StatusCode);
            NenhumaEscritaNoBanco();
        }

        // ---------- validação de entrada (limites da tabela produto) ----------

        public static TheoryData<string, Produto> ProdutosInvalidos => new()
        {
            { "POST", new Produto { Nome = new string('a', 151), Tipo = "Tinto", Preco = 10 } },
            { "POST", new Produto { Nome = " ", Tipo = "Tinto", Preco = 10 } },
            { "POST", new Produto { Nome = "Vinho", Tipo = new string('t', 21), Preco = 10 } },
            { "POST", new Produto { Nome = "Vinho", Tipo = "Tinto", Preco = -1 } },
            { "POST", new Produto { Nome = "Vinho", Tipo = "Tinto", Preco = 100_000_000m } },
            { "PUT", new Produto { Id = 1, Nome = new string('a', 151), Tipo = "Tinto", Preco = 10 } },
        };

        [Theory]
        [MemberData(nameof(ProdutosInvalidos))]
        public async Task Escrita_ComDadosForaDosLimites_Retorna400_SemGravar(string metodo, Produto produto)
        {
            var url = metodo == "PUT" ? Rota + "/1" : Rota;

            var response = await EnviarAsync(metodo, url, TokensDeTeste.Admin(1), produto);

            // Sem a validação, o PostgreSQL recusaria o valor e a API devolveria 500.
            Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
            NenhumaEscritaNoBanco();
        }

        // ---------- tokens inválidos ----------

        public static TheoryData<string> TokensInvalidos => new()
        {
            TokensDeTeste.Expirado(),
            TokensDeTeste.AssinadoPorOutraChave(),
            TokensDeTeste.ComAudience("outro-sistema"),
            TokensDeTeste.ComIssuer("https://identidade-falsa.teste"),
            "isto.nao.e-um-jwt",
        };

        [Theory]
        [MemberData(nameof(TokensInvalidos))]
        public async Task Escrita_ComTokenInvalido_Retorna401(string token)
        {
            var response = await EnviarAsync("POST", Rota, token, new Produto { Nome = "X" });

            Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
            NenhumaEscritaNoBanco();
        }

        [Fact]
        public async Task Health_Retorna200()
        {
            var response = await _factory.CreateClient().GetAsync("/health");

            Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        }

        // ---------- auxiliares ----------

        private async Task<HttpResponseMessage> EnviarAsync(string metodo, string url, string? token, Produto? corpo = null)
        {
            var requisicao = new HttpRequestMessage(new HttpMethod(metodo), url);
            if (token is not null) requisicao.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            if (metodo != "DELETE") requisicao.Content = JsonContent.Create(corpo ?? new Produto { Id = IdDaUrl(url), Nome = "X" });
            // [TokenOptimization] - Um cliente por requisição de teste, descartado logo após o uso.
            return await _factory.CreateClient().SendAsync(requisicao);
        }

        private static long IdDaUrl(string url) => long.TryParse(url.Split('/').Last(), out var id) ? id : 0;

        private void NenhumaEscritaNoBanco()
        {
            _repo.Verify(r => r.CreateAsync(It.IsAny<Produto>()), Times.Never);
            _repo.Verify(r => r.UpdateAsync(It.IsAny<Produto>()), Times.Never);
            _repo.Verify(r => r.DeleteAsync(It.IsAny<long>()), Times.Never);
        }
    }
}
