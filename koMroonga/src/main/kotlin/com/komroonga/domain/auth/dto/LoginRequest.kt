package com.komroonga.domain.auth.dto

data class LoginRequest(
    val username: String = "",
    val password: String = ""
)