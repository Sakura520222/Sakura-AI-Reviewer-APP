package com.sakura_ai_reviewer.feature.issue.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.issue.data.IssueApiService
import com.sakura_ai_reviewer.feature.issue.data.IssueListData
import com.sakura_ai_reviewer.feature.issue.data.IssueStatsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IssueListUiState(
    val issues: ApiResult<IssueListData> = ApiResult.Loading,
    val stats: ApiResult<IssueStatsData> = ApiResult.Loading,
    val searchQuery: String = "",
    val statusFilter: String = "",
    val categoryFilter: String = "",
    val priorityFilter: String = "",
    val currentPage: Int = 1,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class IssueListViewModel @Inject constructor(
    private val issueApiService: IssueApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(IssueListUiState())
    val uiState: StateFlow<IssueListUiState> = _uiState.asStateFlow()

    init {
        loadIssues()
        loadStats()
    }

    private fun loadIssues() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(issues = ApiResult.Loading)
            try {
                val state = _uiState.value
                val response = issueApiService.getIssues(
                    search = state.searchQuery,
                    status = state.statusFilter,
                    category = state.categoryFilter,
                    priority = state.priorityFilter,
                    page = state.currentPage
                )
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(issues = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        issues = ApiResult.Error(response.error ?: "Failed to load issues")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    issues = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val response = issueApiService.getIssueStats()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(stats = ApiResult.Success(response.data))
                }
            } catch (_: Exception) {}
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
        loadIssues()
    }

    fun updateStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(statusFilter = status, currentPage = 1)
        loadIssues()
    }

    fun updateCategoryFilter(category: String) {
        _uiState.value = _uiState.value.copy(categoryFilter = category, currentPage = 1)
        loadIssues()
    }

    fun updatePriorityFilter(priority: String) {
        _uiState.value = _uiState.value.copy(priorityFilter = priority, currentPage = 1)
        loadIssues()
    }

    fun goToPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
        loadIssues()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, currentPage = 1)
            coroutineScope {
                launch { loadIssuesSuspend() }
                launch { loadStatsSuspend() }
            }
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private suspend fun loadIssuesSuspend() {
        _uiState.value = _uiState.value.copy(issues = ApiResult.Loading)
        try {
            val state = _uiState.value
            val response = issueApiService.getIssues(
                search = state.searchQuery,
                status = state.statusFilter,
                category = state.categoryFilter,
                priority = state.priorityFilter,
                page = state.currentPage
            )
            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(issues = ApiResult.Success(response.data))
            } else {
                _uiState.value = _uiState.value.copy(
                    issues = ApiResult.Error(response.error ?: "Failed to load issues")
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                issues = ApiResult.Error(e.toUserMessage())
            )
        }
    }

    private suspend fun loadStatsSuspend() {
        try {
            val response = issueApiService.getIssueStats()
            if (response.success && response.data != null) {
                _uiState.value = _uiState.value.copy(stats = ApiResult.Success(response.data))
            }
        } catch (_: Exception) {}
    }
}
