package com.sleekydz86.videocall.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("participants")
data class Participant(
    @Id
    val id: String? = null,
    val roomId: String,
    val userId: String,
    val userName: String,
    val sessionId: String,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
    val isConnected: Boolean = true
)