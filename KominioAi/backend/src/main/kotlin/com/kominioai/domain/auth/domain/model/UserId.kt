package com.kominioai.domain.auth.domain.model

import java.util.UUID

data class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "User ID cannot be blank" }
        require(value.length <= 36) { "User ID cannot exceed 36 characters" }
    }

    companion object {
        fun fromString(value: String): UserId = UserId(value)
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
    }
}