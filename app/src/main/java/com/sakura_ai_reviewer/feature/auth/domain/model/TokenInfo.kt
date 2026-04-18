package com.sakura_ai_reviewer.feature.auth.domain.model

data class TokenInfo(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: User
)
