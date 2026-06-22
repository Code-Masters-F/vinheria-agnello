package br.com.agnellovinheria.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.agnellovinheria.android.data.entity.Produto
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para a entidade Produto.
 *
 * [TokenOptimization] - Room gera toda a implementação SQL em tempo de compilação.
 * Usamos Flow para que a UI receba atualizações reativas automaticamente,
 * eliminando polling ou listeners manuais.
 *
 * Todas as operações de escrita são suspend functions — devem ser chamadas
 * a partir de um CoroutineScope (ex: viewModelScope no ViewModel).
 */
@Dao
interface ProdutoDao {

    /**
     * Retorna um Flow com todos os produtos, ordenados alfabeticamente.
     * O Flow emite um novo valor sempre que a tabela for modificada.
     * [US1] - Visualizar Inventário
     */
    @Query("SELECT * FROM produtos ORDER BY nome ASC")
    fun getAllProdutos(): Flow<List<Produto>>

    /**
     * Insere um novo produto. Ignora conflitos de ID (segurança extra).
     * [US2] - Cadastrar Produto
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(produto: Produto)

    /**
     * Atualiza um produto existente baseado no ID primário.
     * [US3] - Atualizar Estoque
     */
    @Update
    suspend fun update(produto: Produto)

    /**
     * Remove permanentemente um produto pelo ID primário.
     * [US4] - Remover Produto
     */
    @Delete
    suspend fun delete(produto: Produto)
}
