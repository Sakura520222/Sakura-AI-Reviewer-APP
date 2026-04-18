package com.sakura_ai_reviewer.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.core.auth.AuthState
import com.sakura_ai_reviewer.core.auth.SessionManager
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.auth.data.AuthApiService
import com.sakura_ai_reviewer.feature.auth.data.CallbackRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authApiService: AuthApiService
) : ViewModel() {

    val authState: StateFlow<AuthState> = sessionManager.authState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun initiateGitHubLogin() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authApiService.getMobileAuthUrl()
                if (response.success && response.data != null) {
                    _loginState.value = LoginState.GotAuthUrl(
                        authorizationUrl = response.data.authorizationUrl,
                        state = response.data.state
                    )
                } else {
                    _loginState.value = LoginState.Error(
                        response.error ?: "Failed to get auth URL"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.toUserMessage())
            }
        }
    }

    fun completeOAuthCallback(code: String, state: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authApiService.postCallback(CallbackRequest(code, state))
                if (response.success && response.data != null) {
                    val tokenData = response.data
                    sessionManager.onLoginSuccess(
                        accessToken = tokenData.accessToken,
                        expiresIn = tokenData.expiresIn,
                        role = tokenData.user.role,
                        userId = tokenData.user.userId,
                        username = tokenData.user.sub,
                        avatarUrl = tokenData.user.avatarUrl
                    )
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error(
                        response.error ?: "Authentication failed"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.toUserMessage())
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authApiService.logout()
            } catch (_: Exception) {
                // Ignore network errors on logout
            }
            sessionManager.logout()
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
}

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class GotAuthUrl(val authorizationUrl: String, val state: String) : LoginState()
    data object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
