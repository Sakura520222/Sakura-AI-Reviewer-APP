package com.sakura_ai_reviewer.feature.auth.domain.model

data class User(
    val userId: Int,
    val username: String,
    val role: String,
    val githubId: Int? = null,
    val avatarUrl: String? = null
)
