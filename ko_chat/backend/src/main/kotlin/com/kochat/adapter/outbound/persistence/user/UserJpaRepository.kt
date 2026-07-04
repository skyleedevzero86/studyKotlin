package com.kochat.adapter.outbound.persistence.user

import com.kochat.domain.user.model.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByUsername(username: String): UserJpaEntity?

    fun existsByUsername(username: String): Boolean

    fun deleteByUsername(username: String)

    fun findByStatusOrderByUsernameAsc(status: UserStatus): List<UserJpaEntity>

    fun findByStatusAndIdNotOrderByUsernameAsc(
        status: UserStatus,
        excludeUserId: Long,
        pageable: Pageable,
    ): Page<UserJpaEntity>

    @Query(
        """
        SELECT u FROM UserJpaEntity u
        WHERE u.status = com.kochat.domain.user.model.UserStatus.ACTIVE
          AND u.id <> :excludeUserId
          AND NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :excludeUserId
              AND ub.blocked.id = u.id
              AND ub.isActive = true
          )
        ORDER BY u.username ASC
        """,
    )
    fun findSearchableActiveUsers(excludeUserId: Long, pageable: Pageable): Page<UserJpaEntity>

    @Query(
        """
        SELECT u FROM UserJpaEntity u
        WHERE u.status = com.kochat.domain.user.model.UserStatus.ACTIVE
          AND u.id <> :excludeUserId
          AND NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :excludeUserId
              AND ub.blocked.id = u.id
              AND ub.isActive = true
          )
          AND (
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY u.username ASC
        """,
    )
    fun searchActiveUsers(excludeUserId: Long, query: String, pageable: Pageable): Page<UserJpaEntity>
}
