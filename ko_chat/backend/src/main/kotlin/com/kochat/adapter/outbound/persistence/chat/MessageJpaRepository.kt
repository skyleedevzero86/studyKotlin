package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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
        ORDER BY m.sequenceNumber DESC, m.createdAt DESC
        """,
    )
    fun findLatestMessages(chatRoomId: Long, pageable: Pageable): List<MessageJpaEntity>

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
        "SELECT COALESCE(MAX(m.sequenceNumber), 0) FROM MessageJpaEntity m WHERE m.chatRoom.id = :chatRoomId",
    )
    fun findMaxSequenceNumber(chatRoomId: Long): Long
}
