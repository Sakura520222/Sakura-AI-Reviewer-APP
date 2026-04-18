package com.sakura_ai_reviewer.core.network

import com.sakura_ai_reviewer.core.security.TokenManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenExpiryInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    companion object {
        private const val SAFETY_MARGIN_MS = 5 * 60 * 1000L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val publicPaths = listOf("/health", "/auth/github", "/auth/callback", "/setup/")
        val isPublic = publicPaths.any { path.contains(it) }

        if (!isPublic && tokenManager.isTokenExpired(SAFETY_MARGIN_MS)) {
            return Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(401)
                .message("Token expired")
                .body(
                    "{\"success\":false,\"error\":\"Token expired\"}"
                        .toResponseBody("application/json".toMediaType())
                )
                .build()
        }

        return chain.proceed(request)
    }
}
