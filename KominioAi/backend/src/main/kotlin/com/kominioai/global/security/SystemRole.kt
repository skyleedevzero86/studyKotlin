package com.kominioai.global.security

enum class SystemRole {
    ANONYMOUS,
    USER,
    ADMIN,
    SYSTEM;

    fun isAdmin(): Boolean = this == ADMIN || this == SYSTEM
}