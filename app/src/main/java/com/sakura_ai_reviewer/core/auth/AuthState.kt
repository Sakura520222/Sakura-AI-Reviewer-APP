package com.sakura_ai_reviewer.core.auth

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data object SetupRequired : AuthState()
    data class Authenticated(
        val role: Role,
        val userId: Int,
        val username: String,
        val avatarUrl: String? = null
    ) : AuthState()
}
