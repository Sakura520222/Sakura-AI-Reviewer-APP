package com.sakura_ai_reviewer.core.network

import com.sakura_ai_reviewer.core.security.TokenManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (NetworkConstants.isPublicPath(path)) {
            return chain.proceed(request)
        }

        val token = tokenManager.getToken()
        if (token != null) {
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(authenticatedRequest)
        }

        // No token for protected path — return synthetic 401 instead of sending unauthenticated request
        return Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("No authentication token")
            .body(
                "{\"success\":false,\"error\":\"No authentication token\"}"
                    .toResponseBody("application/json".toMediaType())
            )
            .build()
    }
}
