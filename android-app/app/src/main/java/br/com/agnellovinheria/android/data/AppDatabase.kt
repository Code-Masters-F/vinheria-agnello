package br.com.agnellovinheria.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.agnellovinheria.android.data.dao.ProdutoDao
import br.com.agnellovinheria.android.data.entity.Produto

/**
 * Banco de dados Room — ponto central de acesso ao SQLite local.
 *
 * [TokenOptimization] - Singleton via companion object evita recriação desnecessária
 *  e mantém uma única conexão com o banco durante o ciclo de vida do app.
 *
 * Para incrementar o schema:
 *  1. Adicione as novas entidades no parâmetro `entities`
 *  2. Incremente o `version`
 *  3. Forneça uma Migration ou use `fallbackToDestructiveMigration()` em dev.
 */
@Database(
    entities = [Produto::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun produtoDao(): ProdutoDao

    companion object {
        // @Volatile garante visibilidade imediata em todos as threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna a instância singleton do banco, criando-a na primeira chamada.
         * Thread-safe via synchronized block.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // [TokenOptimization] - Double-checked locking para performance em cenários concorrentes
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "vinheria_agnello.db"
            ).build()
        }
    }
}
