using System.Security.Cryptography.X509Certificates;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using VinheriaApi.Endpoints;
using VinheriaApi.Repositories;
using VinheriaApi.Security;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(opcoes =>
{
    // Permite testar as rotas protegidas pelo Swagger UI colando o token da Identidade.
    opcoes.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        Description = "Token obtido em POST /api/v1/identidade/login (serviço de Identidade).",
    });
    opcoes.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme { Reference = new OpenApiReference { Type = ReferenceType.SecurityScheme, Id = "Bearer" } },
            Array.Empty<string>()
        },
    });
});
builder.Services.AddProblemDetails();
builder.Services.AddHealthChecks();

var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? throw new InvalidOperationException(
        "Connection string 'DefaultConnection' not found. Defina a variável de ambiente ConnectionStrings__DefaultConnection.");

// ---------- Autenticação: tokens emitidos pelo serviço de Identidade ----------
// O Catálogo não conhece senhas nem a chave privada. Com Authority = URL da Identidade, o middleware lê
// /.well-known/openid-configuration, baixa a chave pública do JWKS (pelo DNS, ex.: identidade.vinheria.local)
// e valida assinatura RS256, issuer, audience e expiração de cada token.
var jwt = builder.Configuration.GetSection("Jwt");
var authority = jwt["Authority"]
    ?? throw new InvalidOperationException("Jwt:Authority não configurado. Defina a variável de ambiente Jwt__Authority (URL do serviço de Identidade).");

// CA interna que emitiu o certificado HTTPS da Identidade. Lida na inicialização (falha rápida se o
// caminho estiver errado); sem ela, vale o repositório de certificados do sistema.
var caCertPath = jwt["CaCertPath"];
var caInterna = string.IsNullOrWhiteSpace(caCertPath)
    ? null
    : X509Certificate2.CreateFromPem(File.ReadAllText(caCertPath));

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(opcoes =>
    {
        opcoes.Authority = authority;
        opcoes.Audience = jwt["Audience"] ?? "vinheria";
        // HTTPS obrigatório por padrão; só é desligado explicitamente (Jwt__RequireHttpsMetadata=false)
        // na demonstração de tráfego sem criptografia.
        opcoes.RequireHttpsMetadata = jwt.GetValue("RequireHttpsMetadata", true);
        // Mantém os nomes das claims como emitidos pela Identidade (role, vinheria_id).
        opcoes.MapInboundClaims = false;
        // Token com kid desconhecido (ex.: a Identidade trocou de chave) força nova busca do JWKS, mas a
        // biblioteca limita essas buscas a uma por RefreshInterval (padrão: 5 min). Com 1 min, uma troca de
        // chave deixa de bloquear logins por tanto tempo, sem permitir que tokens forjados sobrecarreguem a Identidade.
        opcoes.RefreshInterval = TimeSpan.FromSeconds(jwt.GetValue("RefreshIntervalSegundos", 60));
        if (caInterna is not null)
        {
            // [TokenOptimization] - Handler criado uma vez e reutilizado pelo cliente HTTP do JWKS.
            opcoes.BackchannelHttpHandler = ValidacaoTls.CriarHandlerConfiandoNaCa(caInterna);
        }
        opcoes.TokenValidationParameters = new TokenValidationParameters
        {
            NameClaimType = "name",
            RoleClaimType = "role",
            ValidAlgorithms = new[] { SecurityAlgorithms.RsaSha256 },
            ClockSkew = TimeSpan.FromSeconds(30),
        };
    });

builder.Services.AddAuthorization(opcoes =>
    opcoes.AddPolicy(ProdutoEndpoints.PoliticaAdmin, politica => politica.RequireRole("Admin")));

// [TokenOptimization] - Repositório com connection string resolvida uma única vez na inicialização.
builder.Services.AddScoped<IProdutoRepository>(_ => new ProdutoRepository(connectionString));

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

app.UseAuthentication();
app.UseAuthorization();

app.MapHealthChecks("/health");
// Versão em execução (APP_VERSAO é definida pelo servidor de aplicação a partir da release publicada).
// [TokenOptimization] - Lida da configuração já carregada, sem acesso a disco ou banco.
app.MapGet("/versao", (IConfiguration configuracao) =>
        Results.Ok(new { servico = "catalogo", versao = configuracao["APP_VERSAO"] ?? "dev" }))
    .WithTags("Infra")
    .AllowAnonymous();
// Rota alinhada à arquitetura de microsserviços + rota original da spec 010, ambas com as mesmas regras.
app.MapProdutoEndpoints("/api/v1/catalogo/produtos", nomear: true);
app.MapProdutoEndpoints("/api/produtos", nomear: false);

app.Run();

public partial class Program { }
