package com.sleekydz86.komfa.domain.user

data class UserRequestDTO(
    val username: String,
    val password: String,
    val email: String?,
)
