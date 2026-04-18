package com.sakura_ai_reviewer.feature.settings.data

import com.sakura_ai_reviewer.core.network.ApiResponse
import com.sakura_ai_reviewer.core.network.EmptyData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface SettingsApiService {

    @GET("settings")
    suspend fun getSettings(): ApiResponse<UserSettingsData>

    @PATCH("settings")
    suspend fun updateSettings(@Body settings: UpdateSettingsRequest): ApiResponse<EmptyData>

    @GET("settings/about")
    suspend fun getAbout(): ApiResponse<AboutData>
}

@JsonClass(generateAdapter = true)
data class UserSettingsData(
    @Json(name = "theme") val theme: String,
    @Json(name = "language") val language: String,
    @Json(name = "items_per_page") val itemsPerPage: Int
)

@JsonClass(generateAdapter = true)
data class UpdateSettingsRequest(
    @Json(name = "theme") val theme: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "items_per_page") val itemsPerPage: Int? = null
)

@JsonClass(generateAdapter = true)
data class AboutData(
    @Json(name = "version") val version: String,
    @Json(name = "build_date") val buildDate: String
)
