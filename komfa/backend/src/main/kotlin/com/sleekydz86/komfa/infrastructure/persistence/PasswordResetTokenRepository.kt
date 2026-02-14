package com.sleekydz86.komfa.infrastructure.persistence

import com.sleekydz86.komfa.domain.user.PasswordResetTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface PasswordResetTokenRepository : JpaRepository<PasswordResetTokenEntity, Long> {
    fun findByTokenValue(tokenValue: String): PasswordResetTokenEntity?
    fun deleteByExpiresAtBefore(instant: Instant): Int
}
