package com.kominioai.domain.auth.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class UserRole(
    val id: UserRoleId,
    val userId: UserId,
    val roleName: String,
    val grantedAt: LocalDateTime,
    val grantedBy: UserId?,
    val active: Boolean
) {
    fun deactivate(): UserRole {
        return copy(active = false)
    }

    fun activate(): UserRole {
        return copy(active = true)
    }

    companion object {
        fun create(
            userId: UserId,
            roleName: String,
            grantedBy: UserId? = null
        ): UserRole {
            return UserRole(
                id = UserRoleId(UUID.randomUUID().toString()),
                userId = userId,
                roleName = roleName,
                grantedAt = LocalDateTime.now(),
                grantedBy = grantedBy,
                active = true
            )
        }

        fun reconstruct(
            id: String,
            userId: String,
            roleName: String,
            grantedAt: LocalDateTime,
            grantedBy: String?,
            active: Boolean
        ): UserRole {
            return UserRole(
                id = UserRoleId(id),
                userId = UserId(userId),
                roleName = roleName,
                grantedAt = grantedAt,
                grantedBy = grantedBy?.let { UserId(it) },
                active = active
            )
        }
    }
}