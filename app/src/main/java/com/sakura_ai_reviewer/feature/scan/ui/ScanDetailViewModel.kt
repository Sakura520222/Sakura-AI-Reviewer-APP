package com.sakura_ai_reviewer.feature.scan.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.scan.data.ScanApiService
import com.sakura_ai_reviewer.feature.scan.data.ScanDetailData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanDetailUiState(
    val scan: ApiResult<ScanDetailData> = ApiResult.Loading,
    val actionResult: String? = null
)

@HiltViewModel
class ScanDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanApiService: ScanApiService
) : ViewModel() {

    private val scanId: Int = savedStateHandle.get<String>("scanId")?.toIntOrNull() ?: -1

    private val _uiState = MutableStateFlow(ScanDetailUiState())
    val uiState: StateFlow<ScanDetailUiState> = _uiState.asStateFlow()

    init {
        loadScanDetail()
    }

    private fun loadScanDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scan = ApiResult.Loading)
            try {
                val response = scanApiService.getScanDetail(scanId)
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(scan = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        scan = ApiResult.Error(response.error ?: "Failed to load scan detail")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    scan = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun retryScan() {
        viewModelScope.launch {
            try {
                val response = scanApiService.retryScan(scanId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(actionResult = "Scan retry initiated")
                    loadScanDetail()
                } else {
                    _uiState.value = _uiState.value.copy(
                        actionResult = response.error ?: "Failed to retry scan"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun cancelScan() {
        viewModelScope.launch {
            try {
                val response = scanApiService.cancelScan(scanId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(actionResult = "Scan cancelled")
                    loadScanDetail()
                } else {
                    _uiState.value = _uiState.value.copy(
                        actionResult = response.error ?: "Failed to cancel scan"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionResult = e.toUserMessage())
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(actionResult = null)
    }

    fun refresh() {
        loadScanDetail()
    }
}
