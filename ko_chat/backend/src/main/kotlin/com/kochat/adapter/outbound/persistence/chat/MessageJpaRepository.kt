package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface MessageJpaRepository : JpaRepository<MessageJpaEntity, Long> {
    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId AND m.isDeleted = false
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
    )
    fun findByChatRoomId(chatRoomId: Long, pageable: Pageable): Page<MessageJpaEntity>

    @Query(
        value = """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND (
          :viewerIsAdmin = true
          OR NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :viewerUserId
              AND ub.blocked.id = m.sender.id
              AND ub.blockedAt <= m.createdAt
              AND (ub.unblockedAt IS NULL OR ub.unblockedAt > m.createdAt)
          )
        )
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
        countQuery = """
        SELECT COUNT(m) FROM MessageJpaEntity m
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND (
          :viewerIsAdmin = true
          OR NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :viewerUserId
              AND ub.blocked.id = m.sender.id
              AND ub.blockedAt <= m.createdAt
              AND (ub.unblockedAt IS NULL OR ub.unblockedAt > m.createdAt)
          )
        )
        """,
    )
    fun findByChatRoomIdVisibleTo(
        chatRoomId: Long,
        viewerUserId: Long,
        viewerIsAdmin: Boolean,
        pageable: Pageable,
    ): Page<MessageJpaEntity>

    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND m.id < :cursor
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
    )
    fun findMessagesBefore(chatRoomId: Long, cursor: Long, pageable: Pageable): List<MessageJpaEntity>

    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND m.id < :cursor
        AND (
          :viewerIsAdmin = true
          OR NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :viewerUserId
              AND ub.blocked.id = m.sender.id
              AND ub.blockedAt <= m.createdAt
              AND (ub.unblockedAt IS NULL OR ub.unblockedAt > m.createdAt)
          )
        )
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
    )
    fun findMessagesBeforeVisibleTo(
        chatRoomId: Long,
        cursor: Long,
        viewerUserId: Long,
        viewerIsAdmin: Boolean,
        pageable: Pageable,
    ): List<MessageJpaEntity>

    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND m.id > :cursor
        ORDER BY m.sequenceNumber ASC, m.createdAt ASC
        """,
    )
    fun findMessagesAfter(chatRoomId: Long, cursor: Long, pageable: Pageable): List<MessageJpaEntity>

    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND m.id > :cursor
        AND (
          :viewerIsAdmin = true
          OR NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :viewerUserId
              AND ub.blocked.id = m.sender.id
              AND ub.blockedAt <= m.createdAt
              AND (ub.unblockedAt IS NULL OR ub.unblockedAt > m.createdAt)
          )
        )
        ORDER BY m.sequenceNumber ASC, m.createdAt ASC
        """,
    )
    fun findMessagesAfterVisibleTo(
        chatRoomId: Long,
        cursor: Long,
        viewerUserId: Long,
        viewerIsAdmin: Boolean,
        pageable: Pageable,
    ): List<MessageJpaEntity>

    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
    )
    fun findLatestMessages(chatRoomId: Long, pageable: Pageable): List<MessageJpaEntity>

    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND (
          :viewerIsAdmin = true
          OR NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :viewerUserId
              AND ub.blocked.id = m.sender.id
              AND ub.blockedAt <= m.createdAt
              AND (ub.unblockedAt IS NULL OR ub.unblockedAt > m.createdAt)
          )
        )
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
    )
    fun findLatestMessagesVisibleTo(
        chatRoomId: Long,
        viewerUserId: Long,
        viewerIsAdmin: Boolean,
        pageable: Pageable,
    ): List<MessageJpaEntity>

    @Query(
        value = """
        SELECT * FROM messages m
        WHERE m.chat_room_id = :chatRoomId AND m.is_deleted = false
        ORDER BY m.sequence_number DESC, m.created_at DESC
        LIMIT 1
        """,
        nativeQuery = true,
    )
    fun findLatestMessage(chatRoomId: Long): MessageJpaEntity?

    @Query(
        """
        SELECT m FROM MessageJpaEntity m
        JOIN FETCH m.sender s
        JOIN FETCH m.chatRoom cr
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND (
          :viewerIsAdmin = true
          OR NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :viewerUserId
              AND ub.blocked.id = m.sender.id
              AND ub.blockedAt <= m.createdAt
              AND (ub.unblockedAt IS NULL OR ub.unblockedAt > m.createdAt)
          )
        )
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
    )
    fun findLatestVisibleMessages(
        chatRoomId: Long,
        viewerUserId: Long,
        viewerIsAdmin: Boolean,
        pageable: Pageable,
    ): List<MessageJpaEntity>

    @Query(
        """
        SELECT COUNT(m) FROM MessageJpaEntity m
        WHERE m.chatRoom.id = :chatRoomId
        AND m.isDeleted = false
        AND m.sender.id <> :viewerUserId
        AND m.createdAt >= :joinedAt
        AND (:lastReadMessageId IS NULL OR m.id > :lastReadMessageId)
        AND (
          :viewerIsAdmin = true
          OR NOT EXISTS (
            SELECT 1 FROM UserBlockJpaEntity ub
            WHERE ub.blocker.id = :viewerUserId
              AND ub.blocked.id = m.sender.id
              AND ub.blockedAt <= m.createdAt
              AND (ub.unblockedAt IS NULL OR ub.unblockedAt > m.createdAt)
          )
        )
        """,
    )
    fun countUnreadVisibleMessages(
        chatRoomId: Long,
        viewerUserId: Long,
        joinedAt: LocalDateTime,
        lastReadMessageId: Long?,
        viewerIsAdmin: Boolean,
    ): Long

    @Query(
        "SELECT COALESCE(MAX(m.sequenceNumber), 0) FROM MessageJpaEntity m WHERE m.chatRoom.id = :chatRoomId",
    )
    fun findMaxSequenceNumber(chatRoomId: Long): Long
}
