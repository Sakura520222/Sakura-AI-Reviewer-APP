package com.sakura_ai_reviewer.feature.issue.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.feature.issue.data.IssueApiService
import com.sakura_ai_reviewer.feature.issue.data.IssueDetailData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IssueDetailUiState(
    val issue: ApiResult<IssueDetailData> = ApiResult.Loading,
    val isReanalyzing: Boolean = false,
    val reanalyzeResult: String? = null
)

@HiltViewModel
class IssueDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val issueApiService: IssueApiService
) : ViewModel() {

    private val issueId: Int = savedStateHandle.get<String>("issueId")?.toIntOrNull() ?: -1

    private val _uiState = MutableStateFlow(IssueDetailUiState())
    val uiState: StateFlow<IssueDetailUiState> = _uiState.asStateFlow()

    init {
        loadIssue()
    }

    private fun loadIssue() {
        viewModelScope.launch {
            try {
                val response = issueApiService.getIssueDetail(issueId)
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(issue = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        issue = ApiResult.Error(response.error ?: "Failed to load issue")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    issue = ApiResult.Error(e.message ?: "Network error")
                )
            }
        }
    }

    fun reanalyze() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReanalyzing = true)
            try {
                val response = issueApiService.reanalyzeIssue(issueId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isReanalyzing = false,
                        reanalyzeResult = "Reanalysis started"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isReanalyzing = false,
                        reanalyzeResult = response.error ?: "Failed to reanalyze"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isReanalyzing = false,
                    reanalyzeResult = e.message ?: "Network error"
                )
            }
        }
    }

    fun retry() {
        _uiState.value = IssueDetailUiState()
        loadIssue()
    }
}
