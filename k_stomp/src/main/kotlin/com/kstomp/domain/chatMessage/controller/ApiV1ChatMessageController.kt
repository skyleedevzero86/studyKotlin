package com.kstomp.domain.chatMessage.controller

import com.kstomp.domain.chatMessage.dto.ChatMessageDto
import com.kstomp.domain.chatMessage.entity.ChatMessage
import com.kstomp.global.StompMessageTemplate
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap


@RestController
@RequestMapping("/api/v1/chat/rooms/{chatRoomId}/messages")
class ApiV1ChatMessageController(
    private val template: RabbitTemplate
) {
    private var lastChatMessageId = 0
    private val chatMessagesByRoomId: MutableMap<Int, MutableList<ChatMessage>> = ConcurrentHashMap()

    init {
        listOf(1, 2, 3).forEach { roomId ->
            val messages = mutableListOf(
                ChatMessage(
                    id = (++lastChatMessageId).toLong(),
                    createDate = LocalDateTime.now(),
                    modifyDate = LocalDateTime.now(),
                    chatRoomId = roomId.toLong(),
                    writerName = "User${roomId}A",
                    content = "Room $roomId 첫 메세지"
                ),
                ChatMessage(
                    id = (++lastChatMessageId).toLong(),
                    createDate = LocalDateTime.now(),
                    modifyDate = LocalDateTime.now(),
                    chatRoomId = roomId.toLong(),
                    writerName = "User${roomId}B",
                    content = "Room $roomId 두번째 메세지"
                )
            )
            chatMessagesByRoomId[roomId] = messages
        }
    }

    @GetMapping
    fun getChatMessages(
        @PathVariable chatRoomId: Int,
        @RequestParam(defaultValue = "-1") afterChatMessageId: Int
    ): List<ChatMessage> {
        val chatMessages = chatMessagesByRoomId[chatRoomId] ?: return emptyList()
        return if (afterChatMessageId == -1) {
            chatMessages
        } else {
            chatMessages.filter { it.id > afterChatMessageId }
        }
    }

    data class ChatMessageWriteReqBody(
        val writerName: String,
        val content: String
    )

    @PostMapping
    fun writeChatMessage(
        @PathVariable chatRoomId: Int,
        @RequestBody reqBody: ChatMessageWriteReqBody
    ): ChatMessage {
        return writeChatMessage(chatRoomId, reqBody.writerName, reqBody.content)
    }

    private fun writeChatMessage(chatRoomId: Int, writerName: String, content: String): ChatMessage {
        val chatMessages = chatMessagesByRoomId.computeIfAbsent(chatRoomId) { mutableListOf() }
        val chatMessage = ChatMessage(
            id = (++lastChatMessageId).toLong(),
            createDate = LocalDateTime.now(),
            modifyDate = LocalDateTime.now(),
            chatRoomId = chatRoomId.toLong(),
            writerName = writerName,
            content = content
        )
        chatMessages.add(chatMessage)
        return chatMessage
    }

    data class CreateMessageReqBody(
        val writerName: String,
        val content: String
    )

    @MessageMapping("/chat/rooms/{chatRoomId}/messages/create")
    fun createMessage(
        createMessageReqBody: CreateMessageReqBody,
        @DestinationVariable chatRoomId: Int
    ) {
        val chatMessages = chatMessagesByRoomId.computeIfAbsent(chatRoomId) { mutableListOf() }
        val chatMessage = ChatMessage(
            id = (++lastChatMessageId).toLong(),
            createDate = LocalDateTime.now(),
            modifyDate = LocalDateTime.now(),
            chatRoomId = chatRoomId.toLong(),
            writerName = createMessageReqBody.writerName,
            content = createMessageReqBody.content
        )
        chatMessages.add(chatMessage)
        val chatMessageDto = ChatMessageDto(chatMessage)
        template.convertAndSend("amq.topic", "chatRooms" + chatRoomId + "MessagesCreated", chatMessageDto);
    }
}