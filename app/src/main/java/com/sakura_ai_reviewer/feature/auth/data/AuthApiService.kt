package com.sakura_ai_reviewer.feature.auth.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @GET("auth/github/mobile")
    suspend fun getMobileAuthUrl(
        @Query("redirect_uri") redirectUri: String? = null
    ): ApiResponse<AuthUrlData>

    @POST("auth/callback")
    suspend fun postCallback(@Body request: CallbackRequest): ApiResponse<TokenData>

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
