package com.komroonga.domain.auth.dto

data class LoginResponse(
    val token: String,
    val username: String,
    val roles: List<String>
)