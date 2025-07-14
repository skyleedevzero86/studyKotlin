package com.kominioai.global.security

enum class UserRole {
    ANONYMOUS,
    USER,
    ADMIN,
    SYSTEM;

    fun isAdmin(): Boolean = this == ADMIN || this == SYSTEM
}