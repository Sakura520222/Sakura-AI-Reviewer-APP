package com.sakura_ai_reviewer.core.network

object NetworkConstants {
    val PUBLIC_PATHS = listOf(
        "/health",
        "/auth/github",
        "/auth/callback",
        "/auth/passkey/",
        "/auth/2fa/",
        "/setup/"
    )

    fun isPublicPath(path: String): Boolean = PUBLIC_PATHS.any { path.contains(it) }
}
