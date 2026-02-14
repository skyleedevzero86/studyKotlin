package com.sleekydz86.komfa.infrastructure.persistence

import com.sleekydz86.komfa.domain.ott.OneTimeTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface OneTimeTokenRepository : JpaRepository<OneTimeTokenEntity, Long> {

    fun findByTokenValue(tokenValue: String): OneTimeTokenEntity?

    @Modifying
    @Query("DELETE FROM OneTimeTokenEntity e WHERE e.expiresAt < :now")
    fun deleteExpiredBefore(now: java.time.Instant): Int
}
