package com.sakura_ai_reviewer.feature.scan.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.core.sse.SseEvent
import com.sakura_ai_reviewer.core.sse.SseManager
import com.sakura_ai_reviewer.feature.scan.data.ScanApiService
import com.sakura_ai_reviewer.feature.scan.data.ScanListData
import com.sakura_ai_reviewer.feature.scan.data.ScanStatsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanListUiState(
    val stats: ApiResult<ScanStatsData> = ApiResult.Loading,
    val scans: ApiResult<ScanListData> = ApiResult.Loading,
    val searchQuery: String = "",
    val statusFilter: String = "",
    val currentPage: Int = 1,
    val actionResult: String? = null
)

@HiltViewModel
class ScanListViewModel @Inject constructor(
    private val scanApiService: ScanApiService,
    private val sseManager: SseManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanListUiState())
    val uiState: StateFlow<ScanListUiState> = _uiState.asStateFlow()

    init {
        loadStats()
        loadScans()
        observeSseEvents()
    }

    private fun observeSseEvents() {
        viewModelScope.launch {
            sseManager.events.collect { event ->
                when (event) {
                    is SseEvent.ScanProgress,
                    is SseEvent.ScanCompleted -> {
                        loadStats()
                        loadScans()
                    }
                    else -> { }
                }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val response = scanApiService.getStats()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(stats = ApiResult.Success(response.data))
                }
            } catch (_: Exception) { }
        }
    }

    private fun loadScans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scans = ApiResult.Loading)
            try {
                val state = _uiState.value
                val response = scanApiService.getScans(
                    search = state.searchQuery,
                    status = state.statusFilter,
                    page = state.currentPage
                )
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(scans = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        scans = ApiResult.Error(response.error ?: "Failed to load scans")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    scans = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
        loadScans()
    }

    fun updateStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(statusFilter = status, currentPage = 1)
        loadScans()
    }

    fun goToPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
        loadScans()
    }

    fun triggerScan() {
        viewModelScope.launch {
            try {
                val response = scanApiService.triggerScan()
                _uiState.value = _uiState.value.copy(
                    actionResult = if (response.success) {
                        "Triggered ${response.data?.count ?: 0} scan(s)"
                    } else {
                        response.error ?: "Failed to trigger scan"
                    }
                )
                if (response.success) { loadStats(); loadScans() }
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
        loadScans()
    }
}
