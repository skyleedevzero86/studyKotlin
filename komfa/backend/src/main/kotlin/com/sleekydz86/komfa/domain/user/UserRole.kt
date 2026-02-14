package com.sleekydz86.komfa.domain.user

enum class UserRole(val authority: String) {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
}
