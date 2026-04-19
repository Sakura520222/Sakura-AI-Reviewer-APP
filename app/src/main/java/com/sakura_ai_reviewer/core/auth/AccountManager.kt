package com.sakura_ai_reviewer.core.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class AccountInfo(
    val userId: Int,
    val username: String,
    val role: String,
    val avatarUrl: String?,
    val token: String,
    val expiresAt: Long
)

@Singleton
class AccountManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AccountManager"
        private const val FILE_NAME = "sakura_accounts_prefs"
        private const val KEY_ACCOUNT_LIST = "account_list"
        private const val KEY_ACTIVE_ACCOUNT_ID = "active_account_id"
        private const val KEY_PREFIX_TOKEN = "account_%d_token"
        private const val KEY_PREFIX_EXPIRES_AT = "account_%d_expires_at"
        private const val KEY_PREFIX_ROLE = "account_%d_role"
        private const val KEY_PREFIX_USERNAME = "account_%d_username"
        private const val KEY_PREFIX_AVATAR_URL = "account_%d_avatar_url"
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

    fun saveAccount(
        userId: Int,
        token: String,
        expiresIn: Long,
        role: String,
        username: String,
        avatarUrl: String?
    ) {
        val expiresAt = System.currentTimeMillis() + expiresIn * 1000
        val userIdStr = userId.toString()

        prefs.edit()
            .putString(KEY_PREFIX_TOKEN.format(userId), token)
            .putLong(KEY_PREFIX_EXPIRES_AT.format(userId), expiresAt)
            .putString(KEY_PREFIX_ROLE.format(userId), role)
            .putString(KEY_PREFIX_USERNAME.format(userId), username)
            .putString(KEY_PREFIX_AVATAR_URL.format(userId), avatarUrl)
            .commit()

        // Add userId to account list
        val accountList = prefs.getStringSet(KEY_ACCOUNT_LIST, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (accountList.add(userIdStr)) {
            prefs.edit().putStringSet(KEY_ACCOUNT_LIST, accountList).commit()
        }

        Log.d(TAG, "Account saved for userId=$userId, username=$username")
    }

    fun setActiveAccount(userId: Int) {
        prefs.edit().putInt(KEY_ACTIVE_ACCOUNT_ID, userId).commit()
        Log.d(TAG, "Active account set to userId=$userId")
    }

    fun getActiveAccountId(): Int {
        return prefs.getInt(KEY_ACTIVE_ACCOUNT_ID, -1)
    }

    fun getAccount(userId: Int): AccountInfo? {
        val token = prefs.getString(KEY_PREFIX_TOKEN.format(userId), null) ?: return null
        val expiresAt = prefs.getLong(KEY_PREFIX_EXPIRES_AT.format(userId), 0L)
        val role = prefs.getString(KEY_PREFIX_ROLE.format(userId), null) ?: return null
        val username = prefs.getString(KEY_PREFIX_USERNAME.format(userId), null) ?: return null
        val avatarUrl = prefs.getString(KEY_PREFIX_AVATAR_URL.format(userId), null)

        return AccountInfo(
            userId = userId,
            username = username,
            role = role,
            avatarUrl = avatarUrl,
            token = token,
            expiresAt = expiresAt
        )
    }

    fun getActiveAccount(): AccountInfo? {
        val activeId = getActiveAccountId()
        return if (activeId != -1) getAccount(activeId) else null
    }

    fun getAccountList(): List<AccountInfo> {
        val userIdSet = prefs.getStringSet(KEY_ACCOUNT_LIST, emptySet()) ?: emptySet()
        return userIdSet.mapNotNull { userIdStr ->
            userIdStr.toIntOrNull()?.let { getAccount(it) }
        }
    }

    fun removeAccount(userId: Int) {
        val userIdStr = userId.toString()

        // Remove all per-account data
        prefs.edit()
            .remove(KEY_PREFIX_TOKEN.format(userId))
            .remove(KEY_PREFIX_EXPIRES_AT.format(userId))
            .remove(KEY_PREFIX_ROLE.format(userId))
            .remove(KEY_PREFIX_USERNAME.format(userId))
            .remove(KEY_PREFIX_AVATAR_URL.format(userId))
            .commit()

        // Remove from account list
        val accountList = prefs.getStringSet(KEY_ACCOUNT_LIST, emptySet())?.toMutableSet() ?: mutableSetOf()
        accountList.remove(userIdStr)

        if (accountList.isEmpty()) {
            // No remaining accounts, clear active account
            prefs.edit()
                .putStringSet(KEY_ACCOUNT_LIST, accountList)
                .remove(KEY_ACTIVE_ACCOUNT_ID)
                .commit()
            Log.d(TAG, "Account removed (userId=$userId), no remaining accounts, active cleared")
        } else {
            prefs.edit().putStringSet(KEY_ACCOUNT_LIST, accountList).commit()

            // If removed account was active, auto-switch to first remaining
            if (getActiveAccountId() == userId) {
                val firstRemaining = accountList.firstOrNull()?.toIntOrNull() ?: -1
                if (firstRemaining != -1) {
                    setActiveAccount(firstRemaining)
                    Log.d(TAG, "Account removed (userId=$userId), auto-switched active to userId=$firstRemaining")
                } else {
                    prefs.edit().remove(KEY_ACTIVE_ACCOUNT_ID).commit()
                    Log.d(TAG, "Account removed (userId=$userId), could not find remaining account, active cleared")
                }
            } else {
                Log.d(TAG, "Account removed (userId=$userId)")
            }
        }
    }

    fun isAccountTokenExpired(userId: Int, safetyMarginMs: Long = 0): Boolean {
        val expiresAt = prefs.getLong(KEY_PREFIX_EXPIRES_AT.format(userId), 0L)
        return System.currentTimeMillis() >= (expiresAt - safetyMarginMs)
    }
}
