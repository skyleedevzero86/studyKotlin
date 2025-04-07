package com.kstomp.domain.chatRoom.entity

import java.time.LocalDateTime

data class ChatRoom(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val name: String
)