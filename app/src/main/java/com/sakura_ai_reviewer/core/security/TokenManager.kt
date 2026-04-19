package com.sakura_ai_reviewer.core.security

import android.util.Log
import com.sakura_ai_reviewer.core.auth.AccountManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val accountManager: AccountManager
) {
    companion object {
        private const val TAG = "TokenManager"
    }

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var cachedExpiresAt: Long = 0L

    init {
        val active = accountManager.getActiveAccount()
        if (active != null) {
            cachedToken = active.token
            cachedExpiresAt = active.expiresAt
        }
    }

    fun saveToken(accessToken: String, expiresIn: Long) {
        val expiresAt = System.currentTimeMillis() + expiresIn * 1000
        cachedToken = accessToken
        cachedExpiresAt = expiresAt
        Log.d(TAG, "Token saved, expires at: $expiresAt")
    }

    fun getToken(): String? {
        Log.d(TAG, "getToken() = ${if (cachedToken != null) "present (${cachedToken!!.take(20)}...)" else "null"}")
        return cachedToken
    }

    fun getTokenExpiresAt(): Long = cachedExpiresAt

    fun isTokenExpired(safetyMarginMs: Long = 0): Boolean {
        val expiresAt = cachedExpiresAt
        return System.currentTimeMillis() >= (expiresAt - safetyMarginMs)
    }

    fun saveUserRole(role: String) { /* No-op: stored via AccountManager */ }

    fun getUserRole(): String? = accountManager.getActiveAccount()?.role

    fun saveUserInfo(userId: Int, username: String, avatarUrl: String?) { /* No-op: stored via AccountManager */ }

    fun getUserId(): Int = accountManager.getActiveAccountId()

    fun getUsername(): String? = accountManager.getActiveAccount()?.username

    fun getAvatarUrl(): String? = accountManager.getActiveAccount()?.avatarUrl

    fun clearAll() {
        cachedToken = null
        cachedExpiresAt = 0L
        Log.d(TAG, "In-memory cache cleared")
    }

    fun refreshFromAccount(userId: Int) {
        val account = accountManager.getAccount(userId)
        if (account != null) {
            cachedToken = account.token
            cachedExpiresAt = account.expiresAt
        } else {
            cachedToken = null
            cachedExpiresAt = 0L
        }
    }
}
