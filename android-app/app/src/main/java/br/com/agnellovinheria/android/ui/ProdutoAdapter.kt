package br.com.agnellovinheria.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.agnellovinheria.android.R
import br.com.agnellovinheria.android.data.entity.Produto

/**
 * Adapter para a RecyclerView de produtos.
 *
 * [TokenOptimization] - Usa ListAdapter com DiffUtil para calcular diffs de forma eficiente,
 * atualizando apenas os itens que mudaram na lista (não a lista inteira).
 */
class ProdutoAdapter(
    private val onEditClick: (Produto) -> Unit,
    private val onDeleteClick: (Produto) -> Unit
) : ListAdapter<Produto, ProdutoAdapter.ProdutoViewHolder>(ProdutoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.bind(getItem(position), onEditClick, onDeleteClick)
    }

    class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvNomeProduto)
        private val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        private val tvEstoque: TextView = itemView.findViewById(R.id.tvEstoque)
        private val tvPreco: TextView = itemView.findViewById(R.id.tvPreco)
        private val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
        private val btnDeletar: ImageButton = itemView.findViewById(R.id.btnDeletar)

        fun bind(produto: Produto, onEdit: (Produto) -> Unit, onDelete: (Produto) -> Unit) {
            tvNome.text = produto.nome
            tvCategoria.text = produto.categoria ?: "Sem categoria"
            tvEstoque.text = "Estoque: ${produto.quantidadeEstoque}"
            tvPreco.text = "R$ %.2f".format(produto.preco)
            btnEditar.setOnClickListener { onEdit(produto) }
            btnDeletar.setOnClickListener { onDelete(produto) }
        }
    }

    /** DiffUtil compara por ID e conteúdo completo para atualizações eficientes */
    class ProdutoDiffCallback : DiffUtil.ItemCallback<Produto>() {
        override fun areItemsTheSame(oldItem: Produto, newItem: Produto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Produto, newItem: Produto) = oldItem == newItem
    }
}
