package br.com.agnellovinheria.android.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.agnellovinheria.android.R
import br.com.agnellovinheria.android.VinheriaApplication
import br.com.agnellovinheria.android.data.entity.Produto
import kotlinx.coroutines.launch

/**
 * Activity principal — tela de inventário de produtos.
 *
 * [TokenOptimization] - Usa repeatOnLifecycle(STARTED) para coletar o StateFlow
 * somente quando a Activity está visível, evitando processamento em background.
 *
 * Fluxo de UI: MainActivity → ProdutoViewModel → ProdutoRepository → ProdutoDao → Room SQLite
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: ProdutoViewModel by viewModels {
        ProdutoViewModel.Factory(
            (application as VinheriaApplication).appContainer.produtoRepository
        )
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddProduto: Button
    private lateinit var adapter: ProdutoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        observeUiState()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewProdutos)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        fabAddProduto = findViewById(R.id.btnAdicionarProduto)

        adapter = ProdutoAdapter(
            onEditClick = { produto -> mostrarDialogEditar(produto) },
            onDeleteClick = { produto -> confirmarDelecao(produto) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabAddProduto.setOnClickListener { mostrarDialogAdicionar() }
    }

    /** [US1] - Observa estado da UI de forma reativa */
    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ProdutoUiState.Loading -> {
                            tvEmptyState.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                        is ProdutoUiState.Success -> {
                            if (state.produtos.isEmpty()) {
                                tvEmptyState.visibility = View.VISIBLE
                                recyclerView.visibility = View.GONE
                            } else {
                                tvEmptyState.visibility = View.GONE
                                recyclerView.visibility = View.VISIBLE
                                adapter.submitList(state.produtos)
                            }
                        }
                        is ProdutoUiState.Error -> {
                            Toast.makeText(this@MainActivity, state.mensagem, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /** [US2] - Dialog para adicionar novo produto */
    private fun mostrarDialogAdicionar() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_produto, null)
        AlertDialog.Builder(this)
            .setTitle("Adicionar Produto")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = dialogView.findViewById<EditText>(R.id.etNome).text.toString()
                val preco = dialogView.findViewById<EditText>(R.id.etPreco).text.toString().toDoubleOrNull() ?: 0.0
                val qtd = dialogView.findViewById<EditText>(R.id.etQuantidade).text.toString().toIntOrNull() ?: 0
                val categoria = dialogView.findViewById<EditText>(R.id.etCategoria).text.toString().ifBlank { null }
                val descricao = dialogView.findViewById<EditText>(R.id.etDescricao).text.toString().ifBlank { null }

                viewModel.inserirProduto(
                    Produto(nome = nome, preco = preco, quantidadeEstoque = qtd, categoria = categoria, descricao = descricao)
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** [US3] - Dialog para editar produto existente */
    private fun mostrarDialogEditar(produto: Produto) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_produto, null)
        dialogView.findViewById<EditText>(R.id.etNome).setText(produto.nome)
        dialogView.findViewById<EditText>(R.id.etPreco).setText(produto.preco.toString())
        dialogView.findViewById<EditText>(R.id.etQuantidade).setText(produto.quantidadeEstoque.toString())
        dialogView.findViewById<EditText>(R.id.etCategoria).setText(produto.categoria ?: "")
        dialogView.findViewById<EditText>(R.id.etDescricao).setText(produto.descricao ?: "")

        AlertDialog.Builder(this)
            .setTitle("Editar Produto")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val atualizado = produto.copy(
                    nome = dialogView.findViewById<EditText>(R.id.etNome).text.toString(),
                    preco = dialogView.findViewById<EditText>(R.id.etPreco).text.toString().toDoubleOrNull() ?: produto.preco,
                    quantidadeEstoque = dialogView.findViewById<EditText>(R.id.etQuantidade).text.toString().toIntOrNull() ?: produto.quantidadeEstoque,
                    categoria = dialogView.findViewById<EditText>(R.id.etCategoria).text.toString().ifBlank { null },
                    descricao = dialogView.findViewById<EditText>(R.id.etDescricao).text.toString().ifBlank { null }
                )
                viewModel.atualizarProduto(atualizado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** [US4] - Confirmação antes de deletar */
    private fun confirmarDelecao(produto: Produto) {
        AlertDialog.Builder(this)
            .setTitle("Remover Produto")
            .setMessage("Tem certeza que deseja remover \"${produto.nome}\"? Esta ação não pode ser desfeita.")
            .setPositiveButton("Remover") { _, _ -> viewModel.deletarProduto(produto) }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
