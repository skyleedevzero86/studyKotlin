package com.kominioai.domain.auth.domain.model

data class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username cannot be blank" }
        require(value.length in 3..100) { "Username must be between 3 and 100 characters" }
        require(value.matches(Regex("^[a-zA-Z0-9_-]+$"))) { "Username can only contain letters, numbers, underscores, and hyphens" }
    }

    companion object {
        fun fromString(value: String): Username = Username(value)
    }
}