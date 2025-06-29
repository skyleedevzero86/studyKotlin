package com.sleekydz86.videocall.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("call_rooms")
data class CallRoom(
    @Id
    val id: String? = null,
    val roomName: String,
    val createdBy: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true,
    val maxParticipants: Int = 2
)