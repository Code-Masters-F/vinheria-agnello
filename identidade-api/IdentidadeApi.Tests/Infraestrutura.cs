using IdentidadeApi.Models;
using IdentidadeApi.Repositories;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;

namespace IdentidadeApi.Tests;

/// <summary>
/// Sobe a API em memória com um repositório falso (sem PostgreSQL) e configuração de teste.
/// </summary>
public sealed class IdentidadeApiFactory : WebApplicationFactory<Program>
{
    public const string Issuer = "http://identidade.teste";
    public const string Audience = "vinheria";

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        // [TokenOptimization] - Configuração injetada uma única vez por fixture, sem depender do ambiente da máquina.
        // A connection string é lida antes do Build() no Program.cs, por isso vai via UseSetting.
        builder.UseSetting("ConnectionStrings:IdentidadeDb", "Host=teste;Database=teste");

        // As opções de JWT são lidas depois do Build(); a fonte em memória entra por último e
        // sobrescreve os valores padrão do appsettings.json.
        builder.ConfigureAppConfiguration((_, config) => config.AddInMemoryCollection(new Dictionary<string, string?>
        {
            ["Jwt:Issuer"] = Issuer,
            ["Jwt:Audience"] = Audience,
            ["Jwt:ExpiracaoMinutos"] = "15",
        }));

        builder.ConfigureServices(services =>
        {
            services.RemoveAll<IUsuarioRepository>();
            services.AddSingleton<IUsuarioRepository>(new UsuarioRepositoryFake());
        });
    }
}

/// <summary>Repositório em memória com os mesmos usuários de demonstração do init.sql.</summary>
public sealed class UsuarioRepositoryFake : IUsuarioRepository
{
    // Work factor 4 (mínimo do BCrypt) apenas para os testes ficarem rápidos.
    public static readonly IReadOnlyList<Usuario> Usuarios = new[]
    {
        new Usuario { Id = 1, VinheriaId = 1, Nome = "Admin Agnello", Email = "admin@agnello.com",
            SenhaHash = BCrypt.Net.BCrypt.HashPassword("admin123", 4), Perfil = Perfis.Admin },
        new Usuario { Id = 2, VinheriaId = 1, Nome = "Cliente Demo", Email = "cliente@agnello.com",
            SenhaHash = BCrypt.Net.BCrypt.HashPassword("cliente123", 4), Perfil = Perfis.Cliente },
        new Usuario { Id = 3, VinheriaId = 1, Nome = "Usuário Inativo", Email = "inativo@agnello.com",
            SenhaHash = BCrypt.Net.BCrypt.HashPassword("inativo123", 4), Perfil = Perfis.Cliente, Ativo = false },
        new Usuario { Id = 4, VinheriaId = 1, Nome = "Hash Corrompido", Email = "corrompido@agnello.com",
            SenhaHash = "isto-nao-e-um-hash-bcrypt", Perfil = Perfis.Cliente },
    };

    public Task<Usuario?> BuscarPorEmailAsync(string email, CancellationToken ct = default)
    {
        // [TokenOptimization] - Busca linear em lista pequena e fixa; sem alocação extra.
        return Task.FromResult(Usuarios.FirstOrDefault(u =>
            string.Equals(u.Email, email, StringComparison.OrdinalIgnoreCase)));
    }
}
