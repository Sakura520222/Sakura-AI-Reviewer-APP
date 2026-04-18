package com.sakura_ai_reviewer.core.auth

enum class Role {
    USER,
    ADMIN,
    SUPER_ADMIN;

    fun canAccess(requiredRole: Role): Boolean = when (requiredRole) {
        USER -> true
        ADMIN -> this == ADMIN || this == SUPER_ADMIN
        SUPER_ADMIN -> this == SUPER_ADMIN
    }

    companion object {
        fun fromString(value: String): Role = when (value) {
            "admin" -> ADMIN
            "super_admin" -> SUPER_ADMIN
            else -> USER
        }
    }
}
