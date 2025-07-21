package com.kominioai.domain.auth.domain.model

import java.util.UUID

data class UserSocialAccountId(val value: String) {
    init {
        require(value.isNotBlank()) { "User social account ID cannot be blank" }
        require(value.length <= 36) { "User social account ID cannot exceed 36 characters" }
    }

    companion object {
        fun fromString(value: String): UserSocialAccountId = UserSocialAccountId(value)
        fun generate(): UserSocialAccountId = UserSocialAccountId(UUID.randomUUID().toString())
    }
}