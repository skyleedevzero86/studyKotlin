package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRoomJpaRepository : JpaRepository<ChatRoomJpaEntity, Long> {
    @Query(
        """
        SELECT DISTINCT cr FROM ChatRoomJpaEntity cr
        JOIN ChatRoomMemberJpaEntity crm ON cr.id = crm.chatRoom.id
        WHERE crm.user.id = :userId AND crm.isActive = true AND cr.isActive = true
        ORDER BY cr.updatedAt DESC
        """,
    )
    fun findUserChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomJpaEntity>

    @Query(
        """
        SELECT DISTINCT cr FROM ChatRoomJpaEntity cr
        JOIN ChatRoomMemberJpaEntity crm ON cr.id = crm.chatRoom.id
        WHERE crm.user.id = :userId AND crm.isActive = true AND cr.isActive = true
          AND (
            LOWER(cr.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(cr.description) LIKE LOWER(CONCAT('%', :query, '%'))
            OR EXISTS (
              SELECT 1 FROM MessageJpaEntity m
              WHERE m.chatRoom.id = cr.id AND m.isDeleted = false
                AND m.content IS NOT NULL
                AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            OR EXISTS (
              SELECT 1 FROM ChatRoomMemberJpaEntity peer
              JOIN peer.user pu
              WHERE peer.chatRoom.id = cr.id AND peer.isActive = true
                AND peer.user.id <> :userId
                AND (
                  LOWER(pu.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
                  OR LOWER(pu.username) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            )
          )
        ORDER BY cr.updatedAt DESC
        """,
    )
    fun searchUserChatRooms(userId: Long, query: String): List<ChatRoomJpaEntity>

    @Query(
        """
        SELECT cr FROM ChatRoomJpaEntity cr
        WHERE cr.type = com.kochat.domain.chat.model.ChatRoomType.DIRECT
          AND cr.isActive = true
          AND EXISTS (
            SELECT 1 FROM ChatRoomMemberJpaEntity m1
            WHERE m1.chatRoom.id = cr.id AND m1.user.id = :userId1 AND m1.isActive = true
          )
          AND EXISTS (
            SELECT 1 FROM ChatRoomMemberJpaEntity m2
            WHERE m2.chatRoom.id = cr.id AND m2.user.id = :userId2 AND m2.isActive = true
          )
        """,
    )
    fun findDirectRoomBetweenUsers(userId1: Long, userId2: Long): List<ChatRoomJpaEntity>

    @Query(
        """
        SELECT cr FROM ChatRoomJpaEntity cr
        WHERE cr.isActive = true
          AND cr.type IN :roomTypes
          AND (:includePrivate = true OR cr.isPrivate = false)
          AND (
            :query IS NULL OR :query = ''
            OR LOWER(cr.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(cr.description) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        """,
    )
    fun discoverPublicChatRooms(
        query: String?,
        roomTypes: List<com.kochat.domain.chat.model.ChatRoomType>,
        includePrivate: Boolean,
        pageable: Pageable,
    ): Page<ChatRoomJpaEntity>

    fun findByIsActiveTrueOrderByUpdatedAtDesc(pageable: Pageable): Page<ChatRoomJpaEntity>
}
