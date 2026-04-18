package com.sakura_ai_reviewer.feature.review.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.feature.review.data.ReviewApiService
import com.sakura_ai_reviewer.feature.review.data.ReviewDetailData
import com.sakura_ai_reviewer.feature.review.data.ReviewFileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewDetailUiState(
    val review: ApiResult<ReviewDetailData> = ApiResult.Loading,
    val files: ApiResult<List<ReviewFileData>> = ApiResult.Loading
)

@HiltViewModel
class ReviewDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reviewApiService: ReviewApiService
) : ViewModel() {

    private val reviewId: Int = savedStateHandle.get<String>("reviewId")?.toIntOrNull() ?: -1

    private val _uiState = MutableStateFlow(ReviewDetailUiState())
    val uiState: StateFlow<ReviewDetailUiState> = _uiState.asStateFlow()

    init {
        loadReview()
        loadFiles()
    }

    private fun loadReview() {
        viewModelScope.launch {
            try {
                val response = reviewApiService.getReviewDetail(reviewId)
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(review = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        review = ApiResult.Error(response.error ?: "Failed to load review")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    review = ApiResult.Error(e.message ?: "Network error")
                )
            }
        }
    }

    private fun loadFiles() {
        viewModelScope.launch {
            try {
                val response = reviewApiService.getReviewFiles(reviewId)
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(files = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        files = ApiResult.Error(response.error ?: "Failed to load files")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    files = ApiResult.Error(e.message ?: "Network error")
                )
            }
        }
    }

    fun retry() {
        _uiState.value = ReviewDetailUiState()
        loadReview()
        loadFiles()
    }
}
