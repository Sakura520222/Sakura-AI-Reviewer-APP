package com.sakura_ai_reviewer.core.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TokenManager"
        private const val FILE_NAME = "sakura_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_AVATAR_URL = "avatar_url"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // In-memory cache for immediate cross-thread access
    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var cachedExpiresAt: Long = 0L

    init {
        // Load from persistent storage on startup
        cachedToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        cachedExpiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0L)
    }

    fun saveToken(accessToken: String, expiresIn: Long) {
        val expiresAt = System.currentTimeMillis() + expiresIn * 1000
        // Update memory cache FIRST for immediate visibility
        cachedToken = accessToken
        cachedExpiresAt = expiresAt
        // Persist synchronously
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
            .commit()
        Log.d(TAG, "Token saved, expires at: $expiresAt")
    }

    fun getToken(): String? {
        val token = cachedToken
        Log.d(TAG, "getToken() = ${if (token != null) "present (${token.take(20)}...)" else "null"}")
        return token
    }

    fun getTokenExpiresAt(): Long = cachedExpiresAt

    fun isTokenExpired(safetyMarginMs: Long = 0): Boolean {
        val expiresAt = cachedExpiresAt
        return System.currentTimeMillis() >= (expiresAt - safetyMarginMs)
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_USER_ROLE, role).commit()
    }

    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun saveUserInfo(userId: Int, username: String, avatarUrl: String?) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_AVATAR_URL, avatarUrl)
            .commit()
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getAvatarUrl(): String? = prefs.getString(KEY_AVATAR_URL, null)

    fun clearAll() {
        cachedToken = null
        cachedExpiresAt = 0L
        prefs.edit().clear().commit()
        Log.d(TAG, "All data cleared")
    }
}
