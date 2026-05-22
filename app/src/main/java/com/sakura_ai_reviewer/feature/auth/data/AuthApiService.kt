package com.sakura_ai_reviewer.feature.auth.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface AuthApiService {

    @GET("auth/github/mobile")
    suspend fun getMobileAuthUrl(
        @Query("redirect_uri") redirectUri: String? = null
    ): ApiResponse<AuthUrlData>

    @POST("auth/callback")
    suspend fun postCallback(@Body request: CallbackRequest): ApiResponse<CallbackResponseData>

    @POST("auth/2fa/verify")
    suspend fun verify2fa(@Body request: Verify2faRequest): ApiResponse<TokenData>

    @POST("auth/2fa/passkey/options")
    suspend fun getPasskeyOptions(@Body request: PasskeyOptionsRequest): ApiResponse<PasskeyOptionsData>

    @POST("auth/2fa/passkey/verify")
    suspend fun verifyPasskey(@Body request: PasskeyVerifyRequest): ApiResponse<TokenData>

    @POST
    suspend fun discoverPasskey(@Url url: String): ApiResponse<PasskeyOptionsData>

    @POST
    suspend fun verifyDiscoverPasskey(
        @Url url: String,
        @Body request: DirectPasskeyVerifyRequest
    ): Response<ApiResponse<WebUiPasskeyVerifyData>>

    @GET("auth/me")
    suspend fun getMeWithToken(
        @Header("Authorization") authorization: String
    ): ApiResponse<UserData>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<EmptyData>

    @GET("auth/me")
    suspend fun getMe(): ApiResponse<UserData>
}

@JsonClass(generateAdapter = true)
data class AuthUrlData(
    @Json(name = "authorization_url") val authorizationUrl: String,
    @Json(name = "state") val state: String
)

@JsonClass(generateAdapter = true)
data class CallbackRequest(
    @Json(name = "code") val code: String,
    @Json(name = "state") val state: String
)

/**
 * OAuth callback 响应可能是直接签发 token 或需要 MFA 验证。
 * - message="ok" 时 data 为 TokenData
 * - message="mfa_required" 时 data 为 MfaRequiredData
 */
@JsonClass(generateAdapter = true)
data class CallbackResponseData(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "user") val user: TokenUserData? = null,
    @Json(name = "mfa_required") val mfaRequired: Boolean? = null,
    @Json(name = "mfa_token") val mfaToken: String? = null,
    @Json(name = "methods") val methods: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class TokenData(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "user") val user: TokenUserData
)

@JsonClass(generateAdapter = true)
data class TokenUserData(
    @Json(name = "sub") val sub: String,
    @Json(name = "role") val role: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "github_id") val githubId: Int? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class UserData(
    @Json(name = "sub") val sub: String,
    @Json(name = "role") val role: String,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "github_id") val githubId: Int? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class Verify2faRequest(
    @Json(name = "mfa_token") val mfaToken: String,
    @Json(name = "code") val code: String
)

@JsonClass(generateAdapter = true)
data class PasskeyOptionsRequest(
    @Json(name = "mfa_token") val mfaToken: String
)

@JsonClass(generateAdapter = true)
data class PasskeyOptionsData(
    @Json(name = "challenge_id") val challengeId: String,
    @Json(name = "public_key") val publicKey: PasskeyPublicKeyData,
    @Json(name = "rp_id") val rpId: String,
    @Json(name = "origin") val origin: String
)

@JsonClass(generateAdapter = true)
data class PasskeyPublicKeyData(
    @Json(name = "challenge") val challenge: String,
    @Json(name = "rpId") val rpId: String,
    @Json(name = "allowCredentials") val allowCredentials: List<Map<String, Any?>>? = null,
    @Json(name = "timeout") val timeout: Int? = null,
    @Json(name = "userVerification") val userVerification: String? = null
) {
    fun toRequestJson(moshi: Moshi): String = moshi.adapter(PasskeyPublicKeyData::class.java).toJson(this)
}

@JsonClass(generateAdapter = true)
data class PasskeyVerifyRequest(
    @Json(name = "mfa_token") val mfaToken: String,
    @Json(name = "challenge_id") val challengeId: String,
    @Json(name = "credential") val credential: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class DirectPasskeyVerifyRequest(
    @Json(name = "challenge_id") val challengeId: String,
    @Json(name = "credential") val credential: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class WebUiPasskeyVerifyData(
    @Json(name = "redirect") val redirect: String? = null
)

