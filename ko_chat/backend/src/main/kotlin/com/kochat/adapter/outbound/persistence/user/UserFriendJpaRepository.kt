package com.kochat.adapter.outbound.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserFriendJpaRepository : JpaRepository<UserFriendJpaEntity, Long> {
    fun findByOwnerIdAndFriendId(ownerId: Long, friendId: Long): UserFriendJpaEntity?

    fun findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(ownerId: Long): List<UserFriendJpaEntity>

    fun existsByOwnerIdAndFriendIdAndIsActiveTrue(ownerId: Long, friendId: Long): Boolean

    @Modifying
    @Query(
        """
        UPDATE UserFriendJpaEntity uf
        SET uf.isActive = false
        WHERE uf.owner.id = :ownerId AND uf.friend.id = :friendId AND uf.isActive = true
        """,
    )
    fun deactivateFriend(ownerId: Long, friendId: Long): Int
}
