package com.kominioai.domain.auth.domain.model

import java.util.UUID

data class UserRoleId(val value: String) {
    init {
        require(value.isNotBlank()) { "User role ID cannot be blank" }
        require(value.length <= 36) { "User role ID cannot exceed 36 characters" }
    }

    companion object {
        fun fromString(value: String): UserRoleId = UserRoleId(value)
        fun generate(): UserRoleId = UserRoleId(UUID.randomUUID().toString())
    }
}