package com.sakura_ai_reviewer.feature.user.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.user.data.UpdateIssueQuotaRequest
import com.sakura_ai_reviewer.feature.user.data.UpdateQuotaRequest
import com.sakura_ai_reviewer.feature.user.data.UpdateRoleRequest
import com.sakura_ai_reviewer.feature.user.data.UserApiService
import com.sakura_ai_reviewer.feature.user.data.UserDetailData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserDetailUiState(
    val user: ApiResult<UserDetailData> = ApiResult.Loading,
    val isUpdating: Boolean = false,
    val updateResult: String? = null
)

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userApiService: UserApiService
) : ViewModel() {

    private val userId: Int = savedStateHandle.get<String>("userId")?.toIntOrNull() ?: -1

    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val response = userApiService.getUserDetail(userId)
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(user = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        user = ApiResult.Error(response.error ?: "Failed to load user")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    user = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun updateRole(role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val response = userApiService.updateUserRole(userId, UpdateRoleRequest(role))
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = if (response.success) "Role updated" else (response.error ?: "Failed")
                )
                if (response.success) loadUser()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = e.toUserMessage()
                )
            }
        }
    }

    fun toggleActive() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val response = userApiService.toggleUser(userId)
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = if (response.success) "Toggled" else (response.error ?: "Failed")
                )
                if (response.success) loadUser()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = e.toUserMessage()
                )
            }
        }
    }

    fun updateQuota(daily: Int, weekly: Int, monthly: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val response = userApiService.updateUserQuota(
                    userId, UpdateQuotaRequest(daily, weekly, monthly)
                )
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = if (response.success) "Quota updated" else (response.error ?: "Failed")
                )
                if (response.success) loadUser()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = e.toUserMessage()
                )
            }
        }
    }

    fun updateIssueQuota(daily: Int, weekly: Int, monthly: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val response = userApiService.updateIssueQuota(
                    userId, UpdateIssueQuotaRequest(daily, weekly, monthly)
                )
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = if (response.success) "Issue quota updated" else (response.error ?: "Failed")
                )
                if (response.success) loadUser()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = e.toUserMessage()
                )
            }
        }
    }

    fun resetQuota() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            try {
                val response = userApiService.resetUserQuota(userId)
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = if (response.success) "Quota reset" else (response.error ?: "Failed")
                )
                if (response.success) loadUser()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateResult = e.toUserMessage()
                )
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(updateResult = null)
    }

    fun retry() {
        _uiState.value = UserDetailUiState()
        loadUser()
    }
}
