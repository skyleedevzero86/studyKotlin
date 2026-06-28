package com.kochat.adapter.outbound.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface UserBlockJpaRepository : JpaRepository<UserBlockJpaEntity, Long> {
    fun findByBlockerIdAndBlockedIdAndIsActiveTrue(blockerId: Long, blockedId: Long): UserBlockJpaEntity?

    fun findByBlockerIdAndIsActiveTrueOrderByBlockedAtDesc(blockerId: Long): List<UserBlockJpaEntity>

    fun findByBlockerIdOrderByBlockedAtDesc(blockerId: Long): List<UserBlockJpaEntity>

    fun existsByBlockerIdAndBlockedIdAndIsActiveTrue(blockerId: Long, blockedId: Long): Boolean

    @Modifying
    @Query(
        """
        UPDATE UserBlockJpaEntity ub
        SET ub.isActive = false, ub.unblockedAt = :unblockedAt
        WHERE ub.blocker.id = :blockerId AND ub.blocked.id = :blockedId AND ub.isActive = true
        """,
    )
    fun deactivateActiveBlocks(blockerId: Long, blockedId: Long, unblockedAt: LocalDateTime): Int

    @Query(
        """
        SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END
        FROM UserBlockJpaEntity ub
        WHERE ub.blocker.id = :blockerId
          AND ub.blocked.id = :blockedId
          AND ub.blockedAt <= :messageCreatedAt
          AND (ub.unblockedAt IS NULL OR ub.unblockedAt > :messageCreatedAt)
        """,
    )
    fun existsBlockCoveringMessage(
        blockerId: Long,
        blockedId: Long,
        messageCreatedAt: LocalDateTime,
    ): Boolean
}
