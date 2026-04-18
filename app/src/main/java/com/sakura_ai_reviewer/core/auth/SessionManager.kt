package com.sakura_ai_reviewer.core.auth

import com.sakura_ai_reviewer.core.security.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun initialize() {
        scope.launch {
            val token = tokenManager.getToken()
            if (token != null && !tokenManager.isTokenExpired()) {
                val cachedRole = tokenManager.getUserRole()
                if (cachedRole != null) {
                    _authState.value = AuthState.Authenticated(
                        role = Role.fromString(cachedRole),
                        userId = tokenManager.getUserId(),
                        username = tokenManager.getUsername() ?: "",
                        avatarUrl = tokenManager.getAvatarUrl()
                    )
                    startTokenExpiryMonitor()
                } else {
                    logout()
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun onLoginSuccess(
        accessToken: String,
        expiresIn: Long,
        role: String,
        userId: Int,
        username: String,
        avatarUrl: String?
    ) {
        tokenManager.saveToken(accessToken, expiresIn)
        tokenManager.saveUserRole(role)
        tokenManager.saveUserInfo(userId, username, avatarUrl)
        _authState.value = AuthState.Authenticated(
            role = Role.fromString(role),
            userId = userId,
            username = username,
            avatarUrl = avatarUrl
        )
        startTokenExpiryMonitor()
    }

    fun logout() {
        tokenManager.clearAll()
        _authState.value = AuthState.Unauthenticated
    }

    fun setSetupRequired() {
        _authState.value = AuthState.SetupRequired
    }

    private fun startTokenExpiryMonitor() {
        scope.launch {
            while (true) {
                delay(60_000L)
                if (tokenManager.isTokenExpired()) {
                    logout()
                    break
                }
            }
        }
    }
}
