package com.sakura_ai_reviewer.feature.log.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.log.data.ActionLogListData
import com.sakura_ai_reviewer.feature.log.data.LogApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActionLogListUiState(
    val logs: ApiResult<ActionLogListData> = ApiResult.Loading,
    val actionFilter: String = "",
    val currentPage: Int = 1
)

@HiltViewModel
class ActionLogListViewModel @Inject constructor(
    private val logApiService: LogApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActionLogListUiState())
    val uiState: StateFlow<ActionLogListUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(logs = ApiResult.Loading)
            try {
                val state = _uiState.value
                val response = logApiService.getActionLogs(
                    action = state.actionFilter,
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

    fun updateActionFilter(action: String) {
        _uiState.value = _uiState.value.copy(actionFilter = action, currentPage = 1)
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
