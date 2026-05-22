package com.sakura_ai_reviewer.feature.config.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ConfigApiService {

    @GET("config/ai-providers")
    suspend fun getAiProviders(): ApiResponse<AiProvidersData>

    @POST("config/ai-providers/{provider}/models")
    suspend fun getProviderModels(
        @Path("provider") provider: String,
        @Body body: ProviderModelsRequest? = null
    ): ApiResponse<ProviderModelsData>

    @GET("config/general")
    suspend fun getGeneralConfig(): ApiResponse<GeneralConfigData>

    @PATCH("config/general")
    suspend fun updateGeneralConfig(
        @Body body: UpdateGeneralConfigRequest
    ): ApiResponse<EmptyData>

    @GET("config/strategies")
    suspend fun getStrategies(): ApiResponse<StrategiesData>

    @PATCH("config/strategies/{section}")
    suspend fun updateStrategy(
        @Path("section") section: String,
        @Body body: UpdateStrategyRequest
    ): ApiResponse<EmptyData>

    @GET("config/labels")
    suspend fun getLabels(): ApiResponse<LabelsData>

    @PUT("config/labels")
    suspend fun updateLabels(
        @Body body: UpdateLabelsRequest
    ): ApiResponse<EmptyData>

    @PATCH("config/labels/recommendation")
    suspend fun updateLabelRecommendation(
        @Body body: UpdateLabelRecommendationRequest
    ): ApiResponse<EmptyData>
}

@JsonClass(generateAdapter = true)
data class AiProvidersData(
    @Json(name = "providers") val providers: List<AiProviderItem>
)

@JsonClass(generateAdapter = true)
data class AiProviderItem(
    @Json(name = "id") val id: String,
    @Json(name = "label") val label: String,
    @Json(name = "base_url") val baseUrl: String? = null,
    @Json(name = "default_model") val defaultModel: String? = null,
    @Json(name = "models_endpoint") val modelsEndpoint: String? = null,
    @Json(name = "model_detail_endpoint") val modelDetailEndpoint: String? = null,
    @Json(name = "supports_model_list") val supportsModelList: Boolean? = null,
    @Json(name = "supports_context_window") val supportsContextWindow: Boolean? = null,
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class ProviderModelsRequest(
    @Json(name = "api_key") val apiKey: String? = null,
    @Json(name = "api_base") val apiBase: String? = null,
    @Json(name = "model") val model: String? = null
)

@JsonClass(generateAdapter = true)
data class ProviderModelsData(
    @Json(name = "success") val success: Boolean,
    @Json(name = "models") val models: List<String>? = null,
    @Json(name = "context_window_k") val contextWindowK: Int? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class GeneralConfigData(
    @Json(name = "configs") val configs: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class UpdateGeneralConfigRequest(
    @Json(name = "configs") val configs: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class StrategiesData(
    @Json(name = "strategies") val strategies: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class UpdateStrategyRequest(
    @Json(name = "data") val data: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class LabelsData(
    @Json(name = "labels") val labels: List<Any>,
    @Json(name = "recommendation") val recommendation: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class UpdateLabelsRequest(
    @Json(name = "labels") val labels: List<Any>
)

@JsonClass(generateAdapter = true)
data class UpdateLabelRecommendationRequest(
    @Json(name = "recommendation") val recommendation: Map<String, Any>
)
