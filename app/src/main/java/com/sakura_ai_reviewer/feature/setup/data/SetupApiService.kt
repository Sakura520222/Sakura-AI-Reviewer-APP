package com.sakura_ai_reviewer.feature.setup.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SetupApiService {

    @GET("setup/state")
    suspend fun getState(): ApiResponse<SetupStateData>

    @POST("setup/test-connection")
    suspend fun testConnection(
        @Body request: TestConnectionRequest
    ): ApiResponse<TestConnectionResultData>

    @POST("setup/save-step")
    suspend fun saveStep(
        @Body request: SaveStepRequest
    ): ApiResponse<SaveStepResultData>

    @POST("setup/complete")
    suspend fun complete(
        @Body request: CompleteSetupRequest
    ): ApiResponse<CompleteSetupResultData>
}

@JsonClass(generateAdapter = true)
data class SetupStateData(
    @Json(name = "state") val state: String,
    @Json(name = "current_step") val currentStep: Int,
    @Json(name = "missing_fields") val missingFields: List<String>,
    @Json(name = "field_groups") val fieldGroups: Map<String, List<FieldDef>>
)

@JsonClass(generateAdapter = true)
data class FieldDef(
    @Json(name = "key") val key: String? = null,
    @Json(name = "label") val label: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "required") val required: Boolean? = null,
    @Json(name = "default") val default: String? = null,
    @Json(name = "placeholder") val placeholder: String? = null
)

@JsonClass(generateAdapter = true)
data class TestConnectionRequest(
    @Json(name = "type") val type: String,
    @Json(name = "database_url") val databaseUrl: String? = null,
    @Json(name = "redis_url") val redisUrl: String? = null,
    @Json(name = "app_id") val appId: String? = null,
    @Json(name = "private_key") val privateKey: String? = null,
    @Json(name = "api_key") val apiKey: String? = null,
    @Json(name = "api_base") val apiBase: String? = null,
    @Json(name = "bot_token") val botToken: String? = null
)

@JsonClass(generateAdapter = true)
data class TestConnectionResultData(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class SaveStepRequest(
    @Json(name = "values") val values: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class SaveStepResultData(
    @Json(name = "saved_count") val savedCount: Int
)

@JsonClass(generateAdapter = true)
data class CompleteSetupRequest(
    @Json(name = "DATABASE_URL") val databaseUrl: String? = null,
    @Json(name = "REDIS_URL") val redisUrl: String? = null,
    @Json(name = "GITHUB_APP_ID") val githubAppId: String? = null,
    @Json(name = "GITHUB_PRIVATE_KEY") val githubPrivateKey: String? = null,
    @Json(name = "GITHUB_WEBHOOK_SECRET") val githubWebhookSecret: String? = null,
    @Json(name = "OPENAI_API_KEY") val openaiApiKey: String? = null,
    @Json(name = "OPENAI_API_BASE") val openaiApiBase: String? = null,
    @Json(name = "OPENAI_MODEL") val openaiModel: String? = null,
    @Json(name = "TELEGRAM_BOT_TOKEN") val telegramBotToken: String? = null,
    @Json(name = "APP_DOMAIN") val appDomain: String? = null,
    @Json(name = "APP_PORT") val appPort: String? = null,
    @Json(name = "LOG_LEVEL") val logLevel: String? = null,
    @Json(name = "ADMIN_GITHUB_USERNAME") val adminGithubUsername: String? = null,
    @Json(name = "ADMIN_TELEGRAM_ID") val adminTelegramId: String? = null,
    @Json(name = "GITHUB_OAUTH_CLIENT_ID") val githubOauthClientId: String? = null,
    @Json(name = "GITHUB_OAUTH_CLIENT_SECRET") val githubOauthClientSecret: String? = null,
    @Json(name = "GITHUB_OAUTH_REDIRECT_URI") val githubOauthRedirectUri: String? = null,
    @Json(name = "EMBEDDING_API_KEY") val embeddingApiKey: String? = null,
    @Json(name = "EMBEDDING_BASE_URL") val embeddingBaseUrl: String? = null,
    @Json(name = "EMBEDDING_MODEL") val embeddingModel: String? = null,
    @Json(name = "EMBEDDING_PROVIDER") val embeddingProvider: String? = null,
    @Json(name = "EMBEDDING_DIMENSION") val embeddingDimension: String? = null,
    @Json(name = "RERANK_API_KEY") val rerankApiKey: String? = null,
    @Json(name = "RERANK_BASE_URL") val rerankBaseUrl: String? = null,
    @Json(name = "RERANK_MODEL") val rerankModel: String? = null,
    @Json(name = "RERANK_PROVIDER") val rerankProvider: String? = null
)

@JsonClass(generateAdapter = true)
data class CompleteSetupResultData(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)
