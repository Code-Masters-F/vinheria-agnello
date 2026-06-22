package br.com.agnellovinheria.android

import android.app.Application
import br.com.agnellovinheria.android.data.AppDatabase
import br.com.agnellovinheria.android.data.repository.ProdutoRepository

/**
 * Service Locator manual para prover dependências ao ViewModel.
 *
 * [TokenOptimization] - Evitamos Hilt/Koin para manter zero dependências externas
 * além das oficiais do Android Jetpack. O AppContainer é criado uma vez na Application
 * e reutilizado por toda a app via referência estática.
 */
class AppContainer(application: Application) {
    private val database = AppDatabase.getInstance(application)
    val produtoRepository = ProdutoRepository(database.produtoDao())
}

/**
 * Application class que inicializa o container de dependências.
 * Registrada no AndroidManifest com android:name=".VinheriaApplication".
 */
class VinheriaApplication : Application() {

    // [TokenOptimization] - lateinit evita inicialização desnecessária antes do onCreate
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
