package br.com.agnellovinheria.android.data.repository

import br.com.agnellovinheria.android.data.dao.ProdutoDao
import br.com.agnellovinheria.android.data.entity.Produto
import kotlinx.coroutines.flow.Flow

/**
 * Repository de Produtos — camada de abstração entre ViewModel e DAO.
 *
 * [TokenOptimization] - Encapsula o acesso ao DAO; o ViewModel não precisa conhecer
 * os detalhes do Room. Isso facilita substituir a fonte de dados no futuro
 * (ex: sincronização remota) sem alterar a camada de UI.
 *
 * Todas as operações de escrita são suspend functions (coroutines),
 * que devem ser chamadas de um CoroutineScope (ex: viewModelScope).
 */
class ProdutoRepository(private val produtoDao: ProdutoDao) {

    /**
     * Fluxo reativo de todos os produtos, ordenados por nome.
     * [TokenOptimization] - Flow observado pela UI: atualiza a lista automaticamente
     * quando o banco é modificado, sem polling manual.
     */
    val todosProdutos: Flow<List<Produto>> = produtoDao.getAllProdutos()

    /**
     * Insere um novo produto no banco local.
     * Lança [IllegalArgumentException] se os dados forem inválidos.
     */
    suspend fun inserir(produto: Produto) {
        validar(produto)
        produtoDao.insert(produto)
    }

    /**
     * Atualiza um produto existente.
     * Lança [IllegalArgumentException] se os dados forem inválidos.
     */
    suspend fun atualizar(produto: Produto) {
        validar(produto)
        produtoDao.update(produto)
    }

    /**
     * Remove permanentemente um produto do banco local.
     */
    suspend fun deletar(produto: Produto) {
        produtoDao.delete(produto)
    }

    /**
     * Valida as regras de negócio antes de persistir.
     * [TokenOptimization] - Centraliza validação aqui para evitar duplicação
     * entre os diferentes casos de uso (inserir/atualizar).
     */
    private fun validar(produto: Produto) {
        require(produto.nome.isNotBlank()) { "O nome do produto não pode ser vazio." }
        require(produto.preco > 0.0) { "O preço deve ser maior que zero." }
        require(produto.quantidadeEstoque >= 0) { "O estoque não pode ser negativo." }
    }
}
