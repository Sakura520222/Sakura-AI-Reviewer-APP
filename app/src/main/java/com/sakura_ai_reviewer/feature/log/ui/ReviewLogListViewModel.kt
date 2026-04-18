package com.sakura_ai_reviewer.feature.log.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.log.data.LogApiService
import com.sakura_ai_reviewer.feature.log.data.ReviewLogListData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewLogListUiState(
    val logs: ApiResult<ReviewLogListData> = ApiResult.Loading,
    val searchQuery: String = "",
    val statusFilter: String = "",
    val currentPage: Int = 1
)

@HiltViewModel
class ReviewLogListViewModel @Inject constructor(
    private val logApiService: LogApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewLogListUiState())
    val uiState: StateFlow<ReviewLogListUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(logs = ApiResult.Loading)
            try {
                val state = _uiState.value
                val response = logApiService.getReviewLogs(
                    search = state.searchQuery,
                    status = state.statusFilter,
                    page = state.currentPage
                )
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(logs = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        logs = ApiResult.Error(response.error ?: "Failed to load logs")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    logs = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
        loadLogs()
    }

    fun updateStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(statusFilter = status, currentPage = 1)
        loadLogs()
    }

    fun goToPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
        loadLogs()
    }

    fun refresh() {
        loadLogs()
    }
}
