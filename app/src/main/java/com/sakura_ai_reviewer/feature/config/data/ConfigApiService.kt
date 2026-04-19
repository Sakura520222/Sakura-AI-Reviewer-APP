package com.sakura_ai_reviewer.feature.config.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface ConfigApiService {

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
}

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
