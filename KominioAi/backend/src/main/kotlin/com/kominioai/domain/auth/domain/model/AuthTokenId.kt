package com.kominioai.domain.auth.domain.model

import java.util.UUID

data class AuthTokenId(val value: String) {
    init {
        require(value.isNotBlank()) { "Auth token ID cannot be blank" }
        require(value.length <= 36) { "Auth token ID cannot exceed 36 characters" }
    }

    companion object {
        fun fromString(value: String): AuthTokenId = AuthTokenId(value)
        fun generate(): AuthTokenId = AuthTokenId(UUID.randomUUID().toString())
    }
}