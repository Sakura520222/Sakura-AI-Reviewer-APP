package com.sakura_ai_reviewer.feature.repo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.repo.data.RepoApiService
import com.sakura_ai_reviewer.feature.repo.data.RepoItemData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepoListUiState(
    val repos: ApiResult<List<RepoItemData>> = ApiResult.Loading,
    val isIndexing: Boolean = false,
    val indexResult: String? = null
)

@HiltViewModel
class RepoListViewModel @Inject constructor(
    private val repoApiService: RepoApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepoListUiState())
    val uiState: StateFlow<RepoListUiState> = _uiState.asStateFlow()

    init {
        loadRepos()
    }

    private fun loadRepos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(repos = ApiResult.Loading)
            try {
                val response = repoApiService.getRepos()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(repos = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        repos = ApiResult.Error(response.error ?: "Failed to load repos")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    repos = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun indexDocs(repoName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIndexing = true)
            try {
                val response = repoApiService.indexDocs(repoName)
                _uiState.value = _uiState.value.copy(
                    isIndexing = false,
                    indexResult = if (response.success) "Docs indexing started" else (response.error ?: "Failed")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isIndexing = false,
                    indexResult = e.toUserMessage()
                )
            }
        }
    }

    fun indexCode(repoName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIndexing = true)
            try {
                val response = repoApiService.indexCode(repoName)
                _uiState.value = _uiState.value.copy(
                    isIndexing = false,
                    indexResult = if (response.success) "Code indexing started" else (response.error ?: "Failed")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isIndexing = false,
                    indexResult = e.toUserMessage()
                )
            }
        }
    }

    fun indexIssues(repoName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIndexing = true)
            try {
                val response = repoApiService.indexIssues(repoName)
                _uiState.value = _uiState.value.copy(
                    isIndexing = false,
                    indexResult = if (response.success) "Issues indexing started" else (response.error ?: "Failed")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isIndexing = false,
                    indexResult = e.toUserMessage()
                )
            }
        }
    }

    fun refresh() {
        loadRepos()
    }
}
