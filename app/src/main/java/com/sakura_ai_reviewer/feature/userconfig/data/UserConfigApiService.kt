package com.sakura_ai_reviewer.feature.userconfig.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserConfigApiService {

    @GET("user-config")
    suspend fun getUserConfig(): ApiResponse<UserConfigData>

    @PATCH("user-config")
    suspend fun updateUserConfig(
        @Body body: UpdateUserConfigRequest
    ): ApiResponse<EmptyData>

    @GET("user-config/metadata")
    suspend fun getUserConfigMetadata(): ApiResponse<UserConfigMetadataData>
}

@JsonClass(generateAdapter = true)
data class UserConfigData(
    @Json(name = "configs") val configs: List<UserConfigItem>
)

@JsonClass(generateAdapter = true)
data class UserConfigItem(
    @Json(name = "key") val key: String,
    @Json(name = "label") val label: String,
    @Json(name = "description") val description: String,
    @Json(name = "input_type") val inputType: String,
    @Json(name = "options") val options: List<UserConfigOption>? = null,
    @Json(name = "user_value") val userValue: String? = null,
    @Json(name = "global_value") val globalValue: String,
    @Json(name = "effective_value") val effectiveValue: String,
    @Json(name = "is_overridden") val isOverridden: Boolean
)

@JsonClass(generateAdapter = true)
data class UserConfigOption(
    @Json(name = "value") val value: String,
    @Json(name = "label") val label: String
)

@JsonClass(generateAdapter = true)
data class UpdateUserConfigRequest(
    @Json(name = "configs") val configs: Map<String, String?>
)

@JsonClass(generateAdapter = true)
data class UserConfigMetadataData(
    @Json(name = "allowed_keys") val allowedKeys: List<String>,
    @Json(name = "options") val options: Map<String, List<UserConfigOption>>
)
