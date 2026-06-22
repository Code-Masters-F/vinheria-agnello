package br.com.agnellovinheria.android.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room que representa um produto físico no estoque da vinheria.
 * [TokenOptimization] - data class simples mapeada diretamente para a tabela SQLite via Room
 *
 * Regras de negócio (validadas no ViewModel/Repository, não no banco):
 *  - nome: não pode ser vazio
 *  - preco: deve ser > 0.0
 *  - quantidadeEstoque: não pode ser < 0
 */
@Entity(tableName = "produtos")
data class Produto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Nome do produto (ex: "Reserva do Porto Special") */
    val nome: String,

    /** Descrição opcional do produto */
    val descricao: String? = null,

    /** Quantidade física em estoque na loja */
    @ColumnInfo(name = "quantidade_estoque")
    val quantidadeEstoque: Int = 0,

    /** Preço unitário em BRL */
    val preco: Double,

    /** Categoria (ex: "Tinto", "Branco", "Rosé", "Acessório") */
    val categoria: String? = null
)
