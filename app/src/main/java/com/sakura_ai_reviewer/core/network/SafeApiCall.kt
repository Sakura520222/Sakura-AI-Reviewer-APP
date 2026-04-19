package com.sakura_ai_reviewer.core.network

/**
 * Executes an API call with standard error handling.
 * Wraps the common pattern: try/catch → check response.success → emit ApiResult.
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> ApiResponse<T>,
    errorMessage: String = "Failed to load data"
): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.success && response.data != null) {
            ApiResult.Success(response.data)
        } else {
            ApiResult.Error(response.error ?: response.message ?: errorMessage)
        }
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage())
    }
}
