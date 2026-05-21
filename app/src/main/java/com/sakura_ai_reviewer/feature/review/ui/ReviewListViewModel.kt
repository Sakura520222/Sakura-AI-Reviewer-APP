package com.sakura_ai_reviewer.feature.review.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.safeApiCall
import com.sakura_ai_reviewer.feature.review.data.ReviewApiService
import com.sakura_ai_reviewer.feature.review.data.ReviewListData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewListUiState(
    val reviews: ApiResult<ReviewListData> = ApiResult.Loading,
    val searchQuery: String = "",
    val statusFilter: String = "",
    val decisionFilter: String = "",
    val currentPage: Int = 1,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    private val reviewApiService: ReviewApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewListUiState())
    val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() = viewModelScope.launch { loadReviewsSuspend() }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
        loadReviews()
    }

    fun updateStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(statusFilter = status, currentPage = 1)
        loadReviews()
    }

    fun updateDecisionFilter(decision: String) {
        _uiState.value = _uiState.value.copy(decisionFilter = decision, currentPage = 1)
        loadReviews()
    }

    fun goToPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
        loadReviews()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, currentPage = 1)
            loadReviewsSuspend()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private suspend fun loadReviewsSuspend() {
        _uiState.value = _uiState.value.copy(reviews = ApiResult.Loading)
        val state = _uiState.value
        _uiState.value = _uiState.value.copy(
            reviews = safeApiCall(
                apiCall = {
                    reviewApiService.getReviews(
                        search = state.searchQuery,
                        status = state.statusFilter,
                        decision = state.decisionFilter,
                        page = state.currentPage
                    )
                },
                errorMessage = "Failed to load reviews"
            )
        )
    }
}
