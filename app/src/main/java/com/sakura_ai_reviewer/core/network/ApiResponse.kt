package com.sakura_ai_reviewer.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "code") val code: String? = null
)

/** Moshi-compatible empty type for endpoints that return no data */
@JsonClass(generateAdapter = true)
class EmptyData
