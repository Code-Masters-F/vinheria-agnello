using IdentidadeApi.Endpoints;
using IdentidadeApi.Repositories;
using IdentidadeApi.Security;
using IdentidadeApi.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddProblemDetails();
builder.Services.AddHealthChecks();

// Configuração de emissão dos tokens, validada na inicialização (falha rápida se faltar algo).
builder.Services.AddOptions<JwtOptions>()
    .Bind(builder.Configuration.GetSection(JwtOptions.Secao))
    .Validate(o => !string.IsNullOrWhiteSpace(o.Issuer) && !string.IsNullOrWhiteSpace(o.Audience),
              "Jwt:Issuer e Jwt:Audience são obrigatórios.")
    .Validate(o => o.ExpiracaoMinutos is > 0 and <= 60, "Jwt:ExpiracaoMinutos deve estar entre 1 e 60.")
    .ValidateOnStart();

// Segredo: vem de variável de ambiente, nunca do appsettings.json.
var connectionString = builder.Configuration.GetConnectionString("IdentidadeDb")
    ?? throw new InvalidOperationException(
        "Connection string 'IdentidadeDb' not found. Defina a variável de ambiente ConnectionStrings__IdentidadeDb.");

// [TokenOptimization] - Chaves, emissor de tokens e relógio como singletons: criados uma vez e reutilizados.
builder.Services.AddSingleton(TimeProvider.System);
builder.Services.AddSingleton<IChaveAssinaturaProvider, ChaveAssinaturaProvider>();
builder.Services.AddSingleton<ITokenService, TokenService>();
builder.Services.AddScoped<IUsuarioRepository>(_ => new UsuarioRepository(connectionString));
builder.Services.AddScoped<IAutenticacaoService, AutenticacaoService>();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
else
{
    // HSTS: em respostas HTTPS, instrui o navegador a nunca mais usar HTTP para este host.
    app.UseHsts();
}

app.MapHealthChecks("/health");
app.MapDescobertaEndpoints();
app.MapIdentidadeEndpoints();

app.Run();

public partial class Program { }
