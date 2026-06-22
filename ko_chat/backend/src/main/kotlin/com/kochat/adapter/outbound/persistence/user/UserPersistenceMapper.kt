package com.kochat.adapter.outbound.persistence.user

import com.kochat.domain.user.model.User
import com.kochat.domain.user.model.UserRole
import com.kochat.domain.user.model.UserStatus
import java.time.Instant

object UserPersistenceMapper {

    fun toDomain(entity: UserJpaEntity): User =
        User.restore(
            id = entity.id,
            username = entity.username ?: "",
            password = entity.password ?: "",
            displayName = entity.displayName,
            role = entity.role ?: UserRole.USER,
            status = entity.status ?: UserStatus.PENDING,
            createdAt = entity.createdAt ?: Instant.EPOCH,
            passwordChangedAt = entity.passwordChangedAt ?: entity.createdAt ?: Instant.EPOCH,
            passwordChangeFailCount = entity.passwordChangeFailCount,
            loginFailCount = entity.loginFailCount,
            lastLoginAt = entity.lastLoginAt,
        )

    fun toEntity(user: User): UserJpaEntity =
        UserJpaEntity().apply {
            id = user.id
            username = user.username
            password = user.password
            displayName = user.displayName
            role = user.role
            status = user.status
            createdAt = user.createdAt
            passwordChangedAt = user.passwordChangedAt
            passwordChangeFailCount = user.passwordChangeFailCount
            loginFailCount = user.loginFailCount
            lastLoginAt = user.lastLoginAt
        }
}
