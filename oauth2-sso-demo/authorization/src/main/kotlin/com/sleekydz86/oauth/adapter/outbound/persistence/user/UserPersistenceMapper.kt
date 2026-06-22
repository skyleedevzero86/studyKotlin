package com.sleekydz86.oauth.adapter.outbound.persistence.user

import com.sleekydz86.oauth.domain.user.model.User
import com.sleekydz86.oauth.domain.user.model.UserRole
import com.sleekydz86.oauth.domain.user.model.UserStatus
import java.time.Instant

object UserPersistenceMapper {

    fun toDomain(entity: UserJpaEntity): User =
        User.restore(
            id = entity.id,
            username = entity.username ?: "",
            password = entity.password ?: "",
            role = entity.role ?: UserRole.USER,
            status = entity.status ?: UserStatus.PENDING,
            createdAt = entity.createdAt ?: Instant.EPOCH,
            passwordChangedAt = entity.passwordChangedAt ?: entity.createdAt ?: Instant.EPOCH,
            passwordChangeFailCount = entity.passwordChangeFailCount,
            lastLoginAt = entity.lastLoginAt,
        )

    fun toEntity(user: User): UserJpaEntity =
        UserJpaEntity().apply {
            id = user.id
            username = user.username
            password = user.password
            role = user.role
            status = user.status
            createdAt = user.createdAt
            passwordChangedAt = user.passwordChangedAt
            passwordChangeFailCount = user.passwordChangeFailCount
            lastLoginAt = user.lastLoginAt
        }
}
