package com.sakura_ai_reviewer.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
    data class Cached<T>(val data: T) : ApiResult<T>()
}

private val errorMoshi: Moshi by lazy {
    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
}

/** Extract human-readable error message from exceptions, including server error bodies */
fun Throwable.toUserMessage(): String {
    return when (this) {
        is HttpException -> {
            val body = response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                try {
                    val adapter = errorMoshi.adapter(ErrorBody::class.java)
                    val errorResponse = adapter.fromJson(body)
                    errorResponse?.error ?: errorResponse?.message ?: message()
                } catch (_: Exception) {
                    // Fallback: try simple JSON extraction
                    extractErrorFromJson(body)
                }
            } else {
                message()
            }
        }
        is IOException -> "Network error"
        else -> message ?: "Unknown error"
    }
}

private fun extractErrorFromJson(json: String): String {
    val errorKey = """"error"\s*:\s*"""
    val match = Regex("""(?<="error"\s*:\s*")([^"]+)""").find(json)
    return match?.groupValues?.get(1) ?: json
}

@JsonClass(generateAdapter = true)
data class ErrorBody(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "message") val message: String? = null
)
