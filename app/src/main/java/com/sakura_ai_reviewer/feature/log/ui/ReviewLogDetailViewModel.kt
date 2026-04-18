package com.sakura_ai_reviewer.feature.log.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.log.data.LogApiService
import com.sakura_ai_reviewer.feature.log.data.ReviewLogDetailData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewLogDetailUiState(
    val log: ApiResult<ReviewLogDetailData> = ApiResult.Loading
)

@HiltViewModel
class ReviewLogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val logApiService: LogApiService
) : ViewModel() {

    private val reviewId: Int = savedStateHandle.get<String>("reviewId")?.toIntOrNull() ?: -1

    private val _uiState = MutableStateFlow(ReviewLogDetailUiState())
    val uiState: StateFlow<ReviewLogDetailUiState> = _uiState.asStateFlow()

    init {
        loadLog()
    }

    private fun loadLog() {
        viewModelScope.launch {
            try {
                val response = logApiService.getReviewLogDetail(reviewId)
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(log = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        log = ApiResult.Error(response.error ?: "Failed to load log")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    log = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun retry() {
        _uiState.value = ReviewLogDetailUiState()
        loadLog()
    }
}
