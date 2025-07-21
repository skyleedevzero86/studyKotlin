package com.kominioai.domain.auth.domain.model

data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.length <= 255) { "Email cannot exceed 255 characters" }
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "Invalid email format"
        }
    }

    companion object {
        fun fromString(value: String): Email = Email(value)
    }
}