package com.sakura_ai_reviewer.core.network

import android.util.Log
import com.sakura_ai_reviewer.core.security.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
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

        val publicPaths = listOf("/health", "/auth/github", "/auth/callback", "/setup/")
        val isPublic = publicPaths.any { path.contains(it) }

        return if (isPublic) {
            Log.d(TAG, "Public path: $path — skipping auth")
            chain.proceed(request)
        } else {
            val token = tokenManager.getToken()
            if (token != null) {
                val authenticatedRequest = request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                Log.d(TAG, "Auth request: $path | Token: ${token.take(20)}... | Headers: ${authenticatedRequest.headers}")
                val response = chain.proceed(authenticatedRequest)
                Log.d(TAG, "Response: ${response.code} for $path | Server headers: ${response.headers}")
                response
            } else {
                Log.w(TAG, "No token available for protected path: $path")
                chain.proceed(request)
            }
        }
    }
}
