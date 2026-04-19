package com.sakura_ai_reviewer.core.network

object NetworkConstants {
    val PUBLIC_PATHS = listOf("/health", "/auth/github", "/auth/callback", "/setup/")

    fun isPublicPath(path: String): Boolean = PUBLIC_PATHS.any { path.contains(it) }
}
