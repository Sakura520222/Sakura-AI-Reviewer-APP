package com.sakura_ai_reviewer.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.auth.AccountInfo
import com.sakura_ai_reviewer.core.auth.AuthState
import com.sakura_ai_reviewer.core.auth.SessionManager
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.dashboard.data.DashboardApiService
import com.sakura_ai_reviewer.feature.dashboard.data.DashboardStatsData
import com.sakura_ai_reviewer.feature.dashboard.data.RecentReviewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val username: String = "",
    val role: String = "",
    val avatarUrl: String? = null,
    val accounts: List<AccountInfo> = emptyList(),
    val activeAccountId: Int = -1,
    val stats: ApiResult<DashboardStatsData> = ApiResult.Loading,
    val recentReviews: ApiResult<List<RecentReviewData>> = ApiResult.Loading,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApiService: DashboardApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadDashboard()
        viewModelScope.launch {
            sessionManager.authState.collect {
                loadUserInfo()
            }
        }
    }

    private fun loadUserInfo() {
        val authState = sessionManager.authState.value
        if (authState is AuthState.Authenticated) {
            _uiState.value = _uiState.value.copy(
                username = authState.username,
                role = authState.role.name.lowercase().replace("_", " "),
                avatarUrl = authState.avatarUrl,
                accounts = sessionManager.getAccountList(),
                activeAccountId = authState.userId
            )
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(stats = ApiResult.Loading)
            try {
                val response = dashboardApiService.getStats()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(
                        stats = ApiResult.Success(response.data)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        stats = ApiResult.Error(response.error ?: "Failed to load stats")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    stats = ApiResult.Error(e.toUserMessage())
                )
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentReviews = ApiResult.Loading)
            try {
                val response = dashboardApiService.getRecentReviews()
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(
                        recentReviews = ApiResult.Success(response.data)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        recentReviews = ApiResult.Error(response.error ?: "Failed to load reviews")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    recentReviews = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                dashboardApiService.refreshCache()
            } catch (_: Exception) {}
            loadDashboard()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun switchAccount(userId: Int) {
        sessionManager.switchAccount(userId)
        loadUserInfo()
        loadDashboard()
    }

    fun removeAccount(userId: Int) {
        sessionManager.removeAccount(userId)
        loadUserInfo()
    }

    fun logout() {
        sessionManager.logout()
    }
}
