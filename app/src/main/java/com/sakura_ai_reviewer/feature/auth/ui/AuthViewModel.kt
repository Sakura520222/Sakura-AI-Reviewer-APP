package com.sakura_ai_reviewer.feature.auth.ui

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura_ai_reviewer.BuildConfig
import com.sakura_ai_reviewer.core.auth.AuthState
import com.sakura_ai_reviewer.core.auth.SessionManager
import com.sakura_ai_reviewer.core.network.toUserMessage
import com.sakura_ai_reviewer.feature.auth.data.AuthApiService
import com.sakura_ai_reviewer.feature.auth.data.CallbackRequest
import com.sakura_ai_reviewer.feature.auth.data.DirectPasskeyVerifyRequest
import com.sakura_ai_reviewer.feature.auth.data.PasskeyOptionsRequest
import com.sakura_ai_reviewer.feature.auth.data.PasskeyVerifyRequest
import com.sakura_ai_reviewer.feature.auth.data.Verify2faRequest
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

private const val MOBILE_OAUTH_REDIRECT_URI = "sakura-ai-reviewer://oauth/callback"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authApiService: AuthApiService,
    val moshi: Moshi
) : ViewModel() {

    val authState: StateFlow<AuthState> = sessionManager.authState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()


    fun initiatePasskeyLogin() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authApiService.discoverPasskey(webUiUrl("auth/passkey/discover"))
                if (response.success && response.data != null) {
                    _loginState.value = LoginState.PasskeyOptionsReady(
                        mfaToken = null,
                        challengeId = response.data.challengeId,
                        publicKey = response.data.publicKey,
                        rpId = response.data.rpId,
                        origin = response.data.origin
                    )
                } else {
                    _loginState.value = LoginState.Error(response.error ?: "Failed to get passkey options")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.toUserMessage())
            }
        }
    }

    fun initiateGitHubLogin(redirectUri: String = MOBILE_OAUTH_REDIRECT_URI) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authApiService.getMobileAuthUrl(redirectUri)
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
                    val data = response.data
                    if (data.mfaRequired == true && data.mfaToken != null) {
                        _loginState.value = LoginState.MfaRequired(
                            mfaToken = data.mfaToken,
                            methods = data.methods ?: emptyList(),
                            username = data.user?.sub ?: "",
                            avatarUrl = data.user?.avatarUrl
                        )
                    } else if (data.accessToken != null && data.user != null) {
                        sessionManager.onLoginSuccess(
                            accessToken = data.accessToken,
                            expiresIn = data.expiresIn ?: 86400L,
                            role = data.user.role,
                            userId = data.user.userId,
                            username = data.user.sub,
                            avatarUrl = data.user.avatarUrl
                        )
                        _loginState.value = LoginState.Success
                    } else {
                        _loginState.value = LoginState.Error("Unexpected response")
                    }
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

    fun verifyTotpCode(mfaToken: String, code: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authApiService.verify2fa(Verify2faRequest(mfaToken, code))
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
                        response.error ?: "Verification failed"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.toUserMessage())
            }
        }
    }

    fun startPasskeyAuth(mfaToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authApiService.getPasskeyOptions(PasskeyOptionsRequest(mfaToken))
                if (response.success && response.data != null) {
                    _loginState.value = LoginState.PasskeyOptionsReady(
                        mfaToken = mfaToken,
                        challengeId = response.data.challengeId,
                        publicKey = response.data.publicKey,
                        rpId = response.data.rpId,
                        origin = response.data.origin
                    )
                } else {
                    _loginState.value = LoginState.Error(
                        response.error ?: "Failed to get passkey options"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.toUserMessage())
            }
        }
    }

    fun verifyPasskey(mfaToken: String?, challengeId: String, credential: Map<String, Any>) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                if (mfaToken != null) {
                    val response = authApiService.verifyPasskey(
                        PasskeyVerifyRequest(mfaToken, challengeId, credential)
                    )
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
                        _loginState.value = LoginState.Error(response.error ?: "Passkey verification failed")
                    }
                } else {
                    completeDirectPasskeyLogin(challengeId, credential)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.toUserMessage())
            }
        }
    }

    private suspend fun completeDirectPasskeyLogin(challengeId: String, credential: Map<String, Any>) {
        val response = authApiService.verifyDiscoverPasskey(
            webUiUrl("auth/passkey/verify-discover"),
            DirectPasskeyVerifyRequest(challengeId, credential)
        )
        if (!response.isSuccessful) {
            _loginState.value = LoginState.Error("Passkey verification failed")
            return
        }
        val token = response.headers().values("Set-Cookie")
            .asSequence()
            .flatMap { it.split(';').asSequence() }
            .map { it.trim() }
            .firstOrNull { it.startsWith("webui_token=") }
            ?.substringAfter('=')
        if (token.isNullOrBlank()) {
            _loginState.value = LoginState.Error("Passkey login did not return a token")
            return
        }
        val claims = tokenPayload(token)
        val expiresIn = (claims.optLong("exp", 0L) - System.currentTimeMillis() / 1000)
            .coerceAtLeast(60L)
        val tokenUser = runCatching { authApiService.getMeWithToken("Bearer $token") }
            .getOrNull()
            ?.takeIf { it.success }
            ?.data
        sessionManager.onLoginSuccess(
            accessToken = token,
            expiresIn = expiresIn,
            role = tokenUser?.role ?: claims.optString("role", "user"),
            userId = tokenUser?.userId ?: claims.optInt("user_id", -1),
            username = tokenUser?.sub ?: claims.optString("sub", ""),
            avatarUrl = tokenUser?.avatarUrl ?: claims.optString("avatar_url").takeIf { it.isNotBlank() }
        )
        _loginState.value = LoginState.Success
    }

    private fun webUiUrl(path: String): String {
        val base = BuildConfig.BASE_URL.removeSuffix("/").removeSuffix("/api/v1")
        return "$base/$path"
    }

    private fun tokenPayload(token: String): JSONObject {
        val payload = token.split('.').getOrNull(1).orEmpty()
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        return JSONObject(json)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authApiService.logout()
            } catch (_: Exception) {
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
    data class MfaRequired(
        val mfaToken: String,
        val methods: List<String>,
        val username: String,
        val avatarUrl: String?
    ) : LoginState()
    data class PasskeyOptionsReady(
        val mfaToken: String?,
        val challengeId: String,
        val publicKey: com.sakura_ai_reviewer.feature.auth.data.PasskeyPublicKeyData,
        val rpId: String,
        val origin: String
    ) : LoginState()
    data class Error(val message: String) : LoginState()
}
