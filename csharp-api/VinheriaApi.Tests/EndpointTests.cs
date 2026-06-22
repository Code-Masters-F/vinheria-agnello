using System.Linq;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Moq;
using System.Net;
using System.Net.Http.Json;
using System.Threading.Tasks;
using VinheriaApi.Models;
using VinheriaApi.Repositories;
using Xunit;

namespace VinheriaApi.Tests
{
    public class EndpointTests : IClassFixture<WebApplicationFactory<Program>>
    {
        private readonly WebApplicationFactory<Program> _factory;
        private readonly Mock<IProdutoRepository> _mockRepo;

        public EndpointTests(WebApplicationFactory<Program> factory)
        {
            _mockRepo = new Mock<IProdutoRepository>();
            _factory = factory.WithWebHostBuilder(builder =>
            {
                builder.ConfigureServices(services =>
                {
                    // Remove existing registration
                    var descriptor = services.SingleOrDefault(d => d.ServiceType == typeof(IProdutoRepository));
                    if (descriptor != null) services.Remove(descriptor);
                    
                    services.AddSingleton(_mockRepo.Object);
                });
            });
        }

        [Fact]
        public async Task GetProdutos_ReturnsOk()
        {
            _mockRepo.Setup(repo => repo.GetAllAsync()).ReturnsAsync(new[] { new Produto { Id = 1, Nome = "Test" } });
            var client = _factory.CreateClient();
            var response = await client.GetAsync("/api/produtos");
            response.EnsureSuccessStatusCode();
            var produtos = await response.Content.ReadFromJsonAsync<Produto[]>();
            Assert.Single(produtos);
        }

        [Fact]
        public async Task GetProdutoById_ReturnsOk()
        {
            _mockRepo.Setup(repo => repo.GetByIdAsync(1)).ReturnsAsync(new Produto { Id = 1, Nome = "Test" });
            var client = _factory.CreateClient();
            var response = await client.GetAsync("/api/produtos/1");
            response.EnsureSuccessStatusCode();
            var produto = await response.Content.ReadFromJsonAsync<Produto>();
            Assert.Equal(1, produto.Id);
        }

        [Fact]
        public async Task CreateProduto_ReturnsCreated()
        {
            _mockRepo.Setup(repo => repo.CreateAsync(It.IsAny<Produto>())).ReturnsAsync(1);
            var client = _factory.CreateClient();
            var response = await client.PostAsJsonAsync("/api/produtos", new Produto { Nome = "Test" });
            Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        }

        [Fact]
        public async Task UpdateProduto_ReturnsNoContent()
        {
            _mockRepo.Setup(repo => repo.GetByIdAsync(1)).ReturnsAsync(new Produto { Id = 1, Nome = "Test" });
            _mockRepo.Setup(repo => repo.UpdateAsync(It.IsAny<Produto>())).ReturnsAsync(true);
            var client = _factory.CreateClient();
            var response = await client.PutAsJsonAsync("/api/produtos/1", new Produto { Id = 1, Nome = "Updated" });
            Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
        }

        [Fact]
        public async Task DeleteProduto_ReturnsNoContent()
        {
            _mockRepo.Setup(repo => repo.GetByIdAsync(1)).ReturnsAsync(new Produto { Id = 1, Nome = "Test" });
            _mockRepo.Setup(repo => repo.DeleteAsync(1)).ReturnsAsync(true);
            var client = _factory.CreateClient();
            var response = await client.DeleteAsync("/api/produtos/1");
            Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);
        }
    }
}
