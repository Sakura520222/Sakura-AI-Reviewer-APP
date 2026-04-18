package com.sakura_ai_reviewer.feature.user.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.network.ApiResult
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.user.data.UserApiService
import com.sakura_ai_reviewer.feature.user.data.UserListData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserListUiState(
    val users: ApiResult<UserListData> = ApiResult.Loading,
    val searchQuery: String = "",
    val roleFilter: String = "",
    val currentPage: Int = 1
)

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userApiService: UserApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(users = ApiResult.Loading)
            try {
                val state = _uiState.value
                val response = userApiService.getUsers(
                    search = state.searchQuery,
                    role = state.roleFilter,
                    page = state.currentPage
                )
                if (response.success && response.data != null) {
                    _uiState.value = _uiState.value.copy(users = ApiResult.Success(response.data))
                } else {
                    _uiState.value = _uiState.value.copy(
                        users = ApiResult.Error(response.error ?: "Failed to load users")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    users = ApiResult.Error(e.toUserMessage())
                )
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
        loadUsers()
    }

    fun updateRoleFilter(role: String) {
        _uiState.value = _uiState.value.copy(roleFilter = role, currentPage = 1)
        loadUsers()
    }

    fun goToPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
        loadUsers()
    }

    fun refresh() {
        loadUsers()
    }
}
