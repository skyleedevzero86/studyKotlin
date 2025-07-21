package com.kominioai.domain.auth.domain.model

data class PasswordHash(val value: String) {
    init {
        require(value.isNotBlank()) { "Password hash cannot be blank" }
        require(value.length <= 255) { "Password hash cannot exceed 255 characters" }
    }

    companion object {
        fun fromString(value: String): PasswordHash = PasswordHash(value)
    }
}