package com.sakura_ai_reviewer.core.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedInterceptor @Inject constructor(
) : Interceptor {

    companion object {
        private const val TAG = "UnauthorizedInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            val path = request.url.encodedPath
            if (!NetworkConstants.isPublicPath(path)) {
                val hasAuthHeader = request.header("Authorization") != null
                Log.w(TAG, "Received 401 for $path, authHeader=$hasAuthHeader")
            }
        }

        return response
    }
}
