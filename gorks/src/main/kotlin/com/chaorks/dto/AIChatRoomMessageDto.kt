package com.chaorks.dto

import com.chaorks.domain.AIChatRoomMessage

data class AIChatRoomMessageDto(
    val id: Long,
    val chatRoomId: Long,
    val createDate: String,
    val modifyDate: String,
    val userMessage: String,
    val botMessage: String
) {
    constructor(message: AIChatRoomMessage) : this(
        id = message.id,
        chatRoomId = message.chatRoom.id,
        createDate = message.createDate.toString(),
        modifyDate = message.modifyDate.toString(),
        userMessage = message.userMessage,
        botMessage = message.botMessage
    )
}