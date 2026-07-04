package com.kochat.adapter.inbound.web.chat.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import java.time.LocalDateTime

@JsonTypeName("MEMBER_READ")
data class MemberReadReceipt(
    val userId: Long,
    val lastReadMessageId: Long?,
    override val chatRoomId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
) : WebSocketMessage()
