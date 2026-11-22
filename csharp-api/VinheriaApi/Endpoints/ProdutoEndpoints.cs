using System.Security.Claims;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Routing;
using VinheriaApi.Models;
using VinheriaApi.Repositories;
using VinheriaApi.Security;

namespace VinheriaApi.Endpoints
{
    /// <summary>
    /// Rotas de produtos do Catálogo.
    /// </summary>
    /// <remarks>
    /// Regras de acesso (RBAC + multi-tenant):
    /// <list type="bullet">
    ///   <item>GET é público (visitantes podem consultar o catálogo);</item>
    ///   <item>POST/PUT/DELETE exigem token válido emitido pela Identidade com perfil <c>Admin</c>;</item>
    ///   <item>o Admin só altera produtos da própria vinheria (<c>vinheria_id</c> do token). Na criação, a
    ///   vinheria do corpo da requisição é ignorada e substituída pela do token.</item>
    /// </list>
    /// </remarks>
    public static class ProdutoEndpoints
    {
        public const string PoliticaAdmin = "Admin";

        /// <summary>
        /// Mapeia as rotas em <paramref name="prefixo"/>. <paramref name="nomear"/> controla o nome dos endpoints
        /// (os nomes precisam ser únicos, então só a rota principal é nomeada).
        /// </summary>
        public static IEndpointRouteBuilder MapProdutoEndpoints(this IEndpointRouteBuilder app, string prefixo, bool nomear)
        {
            var grupo = app.MapGroup(prefixo).WithTags("Produtos");

            var listar = grupo.MapGet("", ListarAsync).AllowAnonymous();
            var obter = grupo.MapGet("/{id:long}", ObterAsync).AllowAnonymous();
            var criar = grupo.MapPost("", CriarAsync).RequireAuthorization(PoliticaAdmin);
            var atualizar = grupo.MapPut("/{id:long}", AtualizarAsync).RequireAuthorization(PoliticaAdmin);
            var excluir = grupo.MapDelete("/{id:long}", ExcluirAsync).RequireAuthorization(PoliticaAdmin);

            if (nomear)
            {
                listar.WithName("GetProdutos").WithOpenApi();
                obter.WithName("GetProdutoById").WithOpenApi();
                criar.WithName("CreateProduto").WithOpenApi();
                atualizar.WithName("UpdateProduto").WithOpenApi();
                excluir.WithName("DeleteProduto").WithOpenApi();
            }
            else
            {
                // Rota legada (spec 010): mesmo comportamento e mesma proteção, fora da documentação.
                grupo.ExcludeFromDescription();
            }

            return app;
        }

        private static async Task<IResult> ListarAsync(IProdutoRepository repositorio)
        {
            // [TokenOptimization] - Uma consulta por requisição, sem filtros adicionais em memória.
            return Results.Ok(await repositorio.GetAllAsync());
        }

        private static async Task<IResult> ObterAsync(long id, IProdutoRepository repositorio)
        {
            var produto = await repositorio.GetByIdAsync(id);
            return produto is not null ? Results.Ok(produto) : Results.NotFound();
        }

        private static async Task<IResult> CriarAsync(Produto produto, ClaimsPrincipal usuario, HttpRequest requisicao,
            IProdutoRepository repositorio)
        {
            if (Validar(produto) is { } erros) return Results.ValidationProblem(erros);
            if (usuario.VinheriaId() is not long vinheriaId) return Results.Forbid();

            produto.VinheriaId = vinheriaId;
            // [TokenOptimization] - Inserção única com RETURNING id, sem consulta extra para obter o id.
            produto.Id = await repositorio.CreateAsync(produto);
            var rota = $"{requisicao.PathBase}{requisicao.Path}".TrimEnd('/');
            return Results.Created($"{rota}/{produto.Id}", produto);
        }

        private static async Task<IResult> AtualizarAsync(long id, Produto produto, ClaimsPrincipal usuario,
            IProdutoRepository repositorio)
        {
            if (id != produto.Id) return Results.BadRequest();
            if (Validar(produto) is { } erros) return Results.ValidationProblem(erros);

            var existente = await repositorio.GetByIdAsync(id);
            if (existente is null) return Results.NotFound();
            if (!PertenceAVinheriaDoUsuario(existente, usuario)) return Results.Forbid();

            // O produto não pode ser "movido" para outra vinheria pelo corpo da requisição.
            produto.VinheriaId = existente.VinheriaId;
            var atualizado = await repositorio.UpdateAsync(produto);
            return atualizado ? Results.NoContent() : Results.NotFound();
        }

        private static async Task<IResult> ExcluirAsync(long id, ClaimsPrincipal usuario, IProdutoRepository repositorio)
        {
            var existente = await repositorio.GetByIdAsync(id);
            if (existente is null) return Results.NotFound();
            if (!PertenceAVinheriaDoUsuario(existente, usuario)) return Results.Forbid();

            var excluido = await repositorio.DeleteAsync(id);
            return excluido ? Results.NoContent() : Results.NotFound();
        }

        // Limites da tabela produto (database/init.sql). Validar antes de gravar devolve 400 com a explicação,
        // em vez de deixar o PostgreSQL recusar o valor e a API responder 500.
        private const int TamanhoMaximoNome = 150;
        private const int TamanhoMaximoTipo = 20;
        private const decimal PrecoMaximo = 99_999_999.99m; // DECIMAL(10,2)

        private static Dictionary<string, string[]>? Validar(Produto produto)
        {
            // [TokenOptimization] - Validação em memória, antes de qualquer acesso ao banco.
            var erros = new Dictionary<string, string[]>();
            if (string.IsNullOrWhiteSpace(produto.Nome) || produto.Nome.Length > TamanhoMaximoNome)
                erros[nameof(Produto.Nome)] = new[] { $"Obrigatório, com até {TamanhoMaximoNome} caracteres." };
            if (string.IsNullOrWhiteSpace(produto.Tipo) || produto.Tipo.Length > TamanhoMaximoTipo)
                erros[nameof(Produto.Tipo)] = new[] { $"Obrigatório, com até {TamanhoMaximoTipo} caracteres." };
            if (produto.Preco < 0 || produto.Preco > PrecoMaximo)
                erros[nameof(Produto.Preco)] = new[] { $"Deve estar entre 0 e {PrecoMaximo}." };
            return erros.Count > 0 ? erros : null;
        }

        private static bool PertenceAVinheriaDoUsuario(Produto produto, ClaimsPrincipal usuario) =>
            usuario.VinheriaId() is long vinheriaId && produto.VinheriaId == vinheriaId;
    }
}
