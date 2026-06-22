package br.com.agnellovinheria.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.agnellovinheria.android.data.entity.Produto
import br.com.agnellovinheria.android.data.repository.ProdutoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gerenciamento do inventário de produtos.
 *
 * [TokenOptimization] - ViewModel sobrevive a rotações de tela (configuration changes),
 * evitando consultas redundantes ao banco. O StateFlow é coletado pela UI de forma reativa.
 *
 * Responsabilidades:
 * - Expor a lista de produtos como StateFlow (US1)
 * - Delegar operações CRUD ao Repository (US2, US3, US4)
 * - Expor mensagens de erro e sucesso via uiState
 */
class ProdutoViewModel(private val repository: ProdutoRepository) : ViewModel() {

    /**
     * Estado da UI: encapsula a lista de produtos e mensagens de feedback.
     * [US1] - Lista reativa de inventário
     */
    private val _uiState = MutableStateFlow<ProdutoUiState>(ProdutoUiState.Loading)
    val uiState: StateFlow<ProdutoUiState> = _uiState.asStateFlow()

    init {
        // [TokenOptimization] - Coleta o Flow do banco no viewModelScope;
        // cancelado automaticamente quando o ViewModel é destruído (sem memory leaks)
        viewModelScope.launch {
            repository.todosProdutos.collect { lista ->
                _uiState.value = ProdutoUiState.Success(lista)
            }
        }
    }

    /** [US2] - Insere novo produto após validação no Repository */
    fun inserirProduto(produto: Produto) {
        viewModelScope.launch {
            runCatching { repository.inserir(produto) }
                .onFailure { _uiState.value = ProdutoUiState.Error(it.message ?: "Erro ao salvar produto.") }
        }
    }

    /** [US3] - Atualiza produto existente */
    fun atualizarProduto(produto: Produto) {
        viewModelScope.launch {
            runCatching { repository.atualizar(produto) }
                .onFailure { _uiState.value = ProdutoUiState.Error(it.message ?: "Erro ao atualizar produto.") }
        }
    }

    /** [US4] - Remove produto permanentemente */
    fun deletarProduto(produto: Produto) {
        viewModelScope.launch {
            runCatching { repository.deletar(produto) }
                .onFailure { _uiState.value = ProdutoUiState.Error(it.message ?: "Erro ao remover produto.") }
        }
    }

    /** Limpa estado de erro após exibição */
    fun limparErro() {
        // Força re-coleta do Flow para voltar ao último estado de sucesso
        viewModelScope.launch {
            repository.todosProdutos.collect { lista ->
                _uiState.value = ProdutoUiState.Success(lista)
            }
        }
    }

    /**
     * Factory necessária para injetar o Repository no ViewModel
     * sem usar frameworks externos de DI.
     * [TokenOptimization] - Reutiliza a mesma instância de Repository do AppContainer
     */
    class Factory(private val repository: ProdutoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProdutoViewModel::class.java)) {
                return ProdutoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/** Sealed class representando os estados possíveis da UI de inventário */
sealed class ProdutoUiState {
    object Loading : ProdutoUiState()
    data class Success(val produtos: List<Produto>) : ProdutoUiState()
    data class Error(val mensagem: String) : ProdutoUiState()
}
