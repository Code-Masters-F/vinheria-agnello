using System.Collections.Generic;
using System.Data;
using System.Threading.Tasks;
using Dapper;
using Npgsql;
using VinheriaApi.Models;

namespace VinheriaApi.Repositories
{
    public class ProdutoRepository : IProdutoRepository
    {
        // As colunas do banco são snake_case (vinheria_id); sem os aliases o Dapper não preenche
        // VinheriaId e todo produto lido voltava com o valor padrão do modelo (1), quebrando a
        // checagem de dono do produto (multi-tenant).
        private const string ColunasProduto =
            "id AS Id, vinheria_id AS VinheriaId, nome AS Nome, tipo AS Tipo, descricao AS Descricao, preco AS Preco, estoque AS Estoque";

        private readonly string _connectionString;

        public ProdutoRepository(string connectionString)
        {
            _connectionString = connectionString;
        }

        private IDbConnection CreateConnection()
        {
            return new NpgsqlConnection(_connectionString);
        }

        public async Task<IEnumerable<Produto>> GetAllAsync()
        {
            using var connection = CreateConnection();
            // [TokenOptimization] - Seleciona só as colunas mapeadas no modelo, em vez de SELECT *.
            return await connection.QueryAsync<Produto>($"SELECT {ColunasProduto} FROM produto ORDER BY id");
        }

        public async Task<Produto?> GetByIdAsync(long id)
        {
            using var connection = CreateConnection();
            return await connection.QueryFirstOrDefaultAsync<Produto>($"SELECT {ColunasProduto} FROM produto WHERE id = @Id", new { Id = id });
        }

        public async Task<long> CreateAsync(Produto produto)
        {
            using var connection = CreateConnection();
            var sql = @"
                INSERT INTO produto (vinheria_id, nome, tipo, descricao, preco, estoque) 
                VALUES (@VinheriaId, @Nome, @Tipo, @Descricao, @Preco, @Estoque)
                RETURNING id;";
            return await connection.ExecuteScalarAsync<long>(sql, produto);
        }

        public async Task<bool> UpdateAsync(Produto produto)
        {
            using var connection = CreateConnection();
            var sql = @"
                UPDATE produto 
                SET vinheria_id = @VinheriaId, nome = @Nome, tipo = @Tipo, descricao = @Descricao, preco = @Preco, estoque = @Estoque
                WHERE id = @Id";
            var affectedRows = await connection.ExecuteAsync(sql, produto);
            return affectedRows > 0;
        }

        public async Task<bool> DeleteAsync(long id)
        {
            using var connection = CreateConnection();
            var affectedRows = await connection.ExecuteAsync("DELETE FROM produto WHERE id = @Id", new { Id = id });
            return affectedRows > 0;
        }
    }
}
