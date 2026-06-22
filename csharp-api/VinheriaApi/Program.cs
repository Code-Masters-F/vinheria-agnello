using VinheriaApi.Models;
using VinheriaApi.Repositories;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") 
    ?? throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");

builder.Services.AddScoped<IProdutoRepository>(sp => new ProdutoRepository(connectionString));

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

// US1 Endpoints
app.MapGet("/api/produtos", async (IProdutoRepository repository) =>
{
    return Results.Ok(await repository.GetAllAsync());
})
.WithName("GetProdutos")
.WithOpenApi();

app.MapGet("/api/produtos/{id:long}", async (long id, IProdutoRepository repository) =>
{
    var produto = await repository.GetByIdAsync(id);
    return produto is not null ? Results.Ok(produto) : Results.NotFound();
})
.WithName("GetProdutoById")
.WithOpenApi();

app.MapPost("/api/produtos", async (Produto produto, IProdutoRepository repository) =>
{
    var id = await repository.CreateAsync(produto);
    produto.Id = id;
    return Results.Created($"/api/produtos/{id}", produto);
})
.WithName("CreateProduto")
.WithOpenApi();

app.MapPut("/api/produtos/{id:long}", async (long id, Produto produto, IProdutoRepository repository) =>
{
    if (id != produto.Id)
    {
        return Results.BadRequest();
    }

    var existingProduto = await repository.GetByIdAsync(id);
    if (existingProduto is null)
    {
        return Results.NotFound();
    }

    var updated = await repository.UpdateAsync(produto);
    return updated ? Results.NoContent() : Results.NotFound();
})
.WithName("UpdateProduto")
.WithOpenApi();

app.MapDelete("/api/produtos/{id:long}", async (long id, IProdutoRepository repository) =>
{
    var existingProduto = await repository.GetByIdAsync(id);
    if (existingProduto is null)
    {
        return Results.NotFound();
    }

    var deleted = await repository.DeleteAsync(id);
    return deleted ? Results.NoContent() : Results.NotFound();
})
.WithName("DeleteProduto")
.WithOpenApi();

app.Run();

public partial class Program { }
