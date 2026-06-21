package com.sleekydz86.oauth.dto

import com.sleekydz86.oauth.domain.user.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 50)
    val username: String,

    @field:NotBlank
    @field:Size(min = 4, max = 100)
    val password: String,

    val role: Role = Role.USER,
)

data class LoginRequest(
    @field:NotBlank
    val username: String,

    @field:NotBlank
    val password: String,
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
)
