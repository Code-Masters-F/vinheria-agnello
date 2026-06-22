package br.com.agnellovinheria.android.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.agnellovinheria.android.data.AppDatabase
import br.com.agnellovinheria.android.data.entity.Produto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Testes TDD para o ProdutoDao usando banco de dados Room em memória.
 *
 * [TokenOptimization] - Banco em memória garante isolamento total entre testes,
 * sem necessidade de limpar dados manualmente nem acessar o disco.
 *
 * ORDEM TDD: estes testes foram escritos ANTES da implementação do DAO.
 * Execute e confirme que FALHAM antes de implementar o DAO.
 */
@RunWith(RobolectricTestRunner::class)
class ProdutoDaoTest {

    // Executa tarefas do LiveData/Architecture Components de forma síncrona nos testes
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var produtoDao: ProdutoDao

    @Before
    fun setup() {
        // [TokenOptimization] - allowMainThreadQueries() apenas para testes; nunca em produção
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        produtoDao = database.produtoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ─── US1: Visualizar Inventário ─────────────────────────────────────────

    @Test
    fun `getAllProdutos retorna lista vazia quando banco esta vazio`() = runTest {
        val produtos = produtoDao.getAllProdutos().first()
        assertTrue("Banco recém-criado deve estar vazio", produtos.isEmpty())
    }

    @Test
    fun `getAllProdutos retorna produto apos insercao`() = runTest {
        val produto = buildProduto(nome = "Reserva do Porto")
        produtoDao.insert(produto)

        val produtos = produtoDao.getAllProdutos().first()
        assertEquals(1, produtos.size)
        assertEquals("Reserva do Porto", produtos[0].nome)
    }

    // ─── US2: Cadastrar Produto ──────────────────────────────────────────────

    @Test
    fun `insert persiste produto e gera id autoincrement`() = runTest {
        val produto = buildProduto(nome = "Chardonnay Branco")
        produtoDao.insert(produto)

        val produtos = produtoDao.getAllProdutos().first()
        assertEquals(1, produtos.size)
        assertTrue("ID deve ser gerado automaticamente", produtos[0].id > 0)
    }

    // ─── US3: Atualizar Estoque ──────────────────────────────────────────────

    @Test
    fun `update altera quantidade de estoque corretamente`() = runTest {
        val produto = buildProduto(nome = "Malbec Argentino", quantidadeEstoque = 10)
        produtoDao.insert(produto)
        val inserido = produtoDao.getAllProdutos().first()[0]

        val atualizado = inserido.copy(quantidadeEstoque = 5)
        produtoDao.update(atualizado)

        val resultado = produtoDao.getAllProdutos().first()[0]
        assertEquals(5, resultado.quantidadeEstoque)
    }

    // ─── US4: Remover Produto ────────────────────────────────────────────────

    @Test
    fun `delete remove produto do banco permanentemente`() = runTest {
        val produto = buildProduto(nome = "Rose Premium")
        produtoDao.insert(produto)
        val inserido = produtoDao.getAllProdutos().first()[0]

        produtoDao.delete(inserido)

        val produtos = produtoDao.getAllProdutos().first()
        assertTrue("Produto deve ser removido", produtos.isEmpty())
    }

    @Test
    fun `delete nao afeta outros produtos`() = runTest {
        produtoDao.insert(buildProduto(nome = "Produto A"))
        produtoDao.insert(buildProduto(nome = "Produto B"))
        val lista = produtoDao.getAllProdutos().first()

        produtoDao.delete(lista[0])

        val restantes = produtoDao.getAllProdutos().first()
        assertEquals(1, restantes.size)
        assertFalse("Produto A deve ter sido removido", restantes.any { it.nome == "Produto A" })
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Cria um Produto de teste com valores padrão sensatos */
    private fun buildProduto(
        nome: String = "Produto Teste",
        quantidadeEstoque: Int = 10,
        preco: Double = 99.90,
        categoria: String? = "Tinto"
    ) = Produto(
        nome = nome,
        quantidadeEstoque = quantidadeEstoque,
        preco = preco,
        categoria = categoria
    )
}
