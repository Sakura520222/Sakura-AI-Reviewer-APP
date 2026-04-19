package com.sakura_ai_reviewer.core.auth

import com.sakura_ai_reviewer.core.security.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val tokenManager: TokenManager,
    private val accountManager: AccountManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var tokenMonitorJob: Job? = null

    fun initialize() {
        scope.launch {
            val activeAccount = accountManager.getActiveAccount()
            if (activeAccount != null && !accountManager.isAccountTokenExpired(activeAccount.userId)) {
                tokenManager.refreshFromAccount(activeAccount.userId)
                _authState.value = AuthState.Authenticated(
                    role = Role.fromString(activeAccount.role),
                    userId = activeAccount.userId,
                    username = activeAccount.username,
                    avatarUrl = activeAccount.avatarUrl
                )
                startTokenExpiryMonitor()
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
        accountManager.saveAccount(userId, accessToken, expiresIn, role, username, avatarUrl)
        accountManager.setActiveAccount(userId)
        tokenManager.saveToken(accessToken, expiresIn)
        _authState.value = AuthState.Authenticated(
            role = Role.fromString(role),
            userId = userId,
            username = username,
            avatarUrl = avatarUrl
        )
        startTokenExpiryMonitor()
    }

    fun logout() {
        val currentUserId = accountManager.getActiveAccountId()
        if (currentUserId != -1) {
            accountManager.removeAccount(currentUserId)
        }
        tokenManager.clearAll()
        val remaining = accountManager.getAccountList()
        if (remaining.isNotEmpty()) {
            switchAccount(remaining.first().userId)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun setSetupRequired() {
        _authState.value = AuthState.SetupRequired
    }

    fun switchAccount(userId: Int): Boolean {
        val account = accountManager.getAccount(userId)
        if (account == null || accountManager.isAccountTokenExpired(userId)) {
            return false
        }
        accountManager.setActiveAccount(userId)
        tokenManager.refreshFromAccount(userId)
        _authState.value = AuthState.Authenticated(
            role = Role.fromString(account.role),
            userId = account.userId,
            username = account.username,
            avatarUrl = account.avatarUrl
        )
        startTokenExpiryMonitor()
        return true
    }

    fun removeAccount(userId: Int) {
        accountManager.removeAccount(userId)
        if (accountManager.getActiveAccountId() == -1) {
            tokenManager.clearAll()
            val remaining = accountManager.getAccountList()
            if (remaining.isNotEmpty()) {
                switchAccount(remaining.first().userId)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun getAccountList(): List<AccountInfo> = accountManager.getAccountList()

    fun getActiveAccountId(): Int = accountManager.getActiveAccountId()

    private fun startTokenExpiryMonitor() {
        tokenMonitorJob?.cancel()
        tokenMonitorJob = scope.launch {
            while (true) {
                delay(60_000L)
                val activeId = accountManager.getActiveAccountId()
                if (activeId != -1 && accountManager.isAccountTokenExpired(activeId)) {
                    logout()
                    break
                }
            }
        }
    }
}
