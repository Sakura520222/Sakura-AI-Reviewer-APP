package com.sakura_ai_reviewer.feature.queue.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.queue.data.QueueApiService
import com.sakura_ai_reviewer.feature.queue.data.QueueListData
import com.sakura_ai_reviewer.feature.queue.data.QueueStatsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QueueUiState(
    val stats: ApiResult<QueueStatsData> = ApiResult.Loading,
    val items: ApiResult<QueueListData> = ApiResult.Loading,
    val searchQuery: String = "",
    val statusFilter: String = "",
    val currentPage: Int = 1,
    val actionResult: String? = null
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val queueApiService: QueueApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    init {
        loadStats()
        loadItems()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val response = queueApiService.getStats()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(stats = ApiResult.Success(response.data))
                }
            } catch (_: Exception) { }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(items = ApiResult.Loading)
            try {
                val state = _uiState.value
                val response = queueApiService.getItems(
                    search = state.searchQuery,
                    status = state.statusFilter,
                    page = state.currentPage
                )
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(items = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        items = ApiResult.Error(response.error ?: "Failed to load queue")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    items = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
        loadItems()
    }

    fun updateStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(statusFilter = status, currentPage = 1)
        loadItems()
    }

    fun goToPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
        loadItems()
    }

    fun retryItem(itemId: Int) {
        viewModelScope.launch {
            try {
                val response = queueApiService.retryItem(itemId)
                _uiState.value = _uiState.value.copy(
                    actionResult = if (response.success) "Item retried" else (response.error ?: "Failed")
                )
                if (response.success) { loadStats(); loadItems() }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            try {
                val response = queueApiService.deleteItem(itemId)
                _uiState.value = _uiState.value.copy(
                    actionResult = if (response.success) "Item deleted" else (response.error ?: "Failed")
                )
                if (response.success) { loadStats(); loadItems() }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun purge(status: String = "completed") {
        viewModelScope.launch {
            try {
                val response = queueApiService.purge(status)
                _uiState.value = _uiState.value.copy(
                    actionResult = if (response.success) "Purged" else (response.error ?: "Failed")
                )
                if (response.success) { loadStats(); loadItems() }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(actionResult = null)
    }

    fun refresh() {
        loadStats()
        loadItems()
    }
}
