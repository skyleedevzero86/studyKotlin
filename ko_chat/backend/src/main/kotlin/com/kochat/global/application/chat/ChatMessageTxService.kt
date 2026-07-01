package com.kochat.global.application.chat

import com.kochat.adapter.inbound.web.chat.dto.ChatMessage
import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaEntity
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaEntity
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaRepository
import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaEntity
import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaRepository
import com.kochat.adapter.outbound.persistence.chat.MessageJpaEntity
import com.kochat.adapter.outbound.persistence.chat.MessageJpaRepository
import com.kochat.adapter.outbound.persistence.chat.MessageSequenceService
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.chat.model.MessageType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ChatMessageTxService(
    private val chatRoomJpaRepository: ChatRoomJpaRepository,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
    private val messageJpaRepository: MessageJpaRepository,
    private val messageAttachmentJpaRepository: MessageAttachmentJpaRepository,
    private val messageSequenceService: MessageSequenceService,
    private val messageMetadataMapper: MessageMetadataMapper,
) {
    @Transactional
    fun saveMessage(
        chatRoom: ChatRoomJpaEntity,
        sender: UserJpaEntity,
        senderMember: ChatRoomMemberJpaEntity,
        prepared: PreparedChatMessage,
    ): SavedChatMessage {
        val roomId = prepared.chatRoomId
        val sequenceNumber = messageSequenceService.getNextSequence(roomId)

        val message = MessageJpaEntity().apply {
            content = prepared.content
            type = prepared.type
            metadata = prepared.metadataJson
            this.chatRoom = chatRoom
            this.sender = sender
            this.sequenceNumber = sequenceNumber
        }
        val savedMessage = messageJpaRepository.save(message)

        var savedAttachment: MessageAttachmentJpaEntity? = null
        if (prepared.type == MessageType.IMAGE || prepared.type == MessageType.FILE) {
            val metadata = messageMetadataMapper.fromJson(prepared.metadataJson)
                ?: throw IllegalArgumentException("첨부 metadata 형식이 올바르지 않습니다.")
            savedAttachment = messageAttachmentJpaRepository.save(
                MessageAttachmentJpaEntity().apply {
                    messageId = savedMessage.id
                    chatRoomId = roomId
                    objectKey = metadata.objectKey ?: throw IllegalArgumentException("objectKey가 필요합니다.")
                    fileName = metadata.fileName ?: "file"
                    mimeType = metadata.mimeType ?: "application/octet-stream"
                    size = metadata.size ?: 0
                },
            )
        }

        val messageId = savedMessage.id ?: throw IllegalArgumentException("메시지 저장에 실패했습니다")
        senderMember.lastReadMessageId = messageId
        chatRoomMemberJpaRepository.save(senderMember)

        chatRoom.updatedAt = LocalDateTime.now()
        chatRoomJpaRepository.save(chatRoom)

        val senderId = sender.id ?: throw IllegalArgumentException("발신자 ID가 없습니다")
        val chatMessage = ChatMessage(
            id = messageId,
            content = savedMessage.content ?: "",
            messageType = savedMessage.type,
            metadata = savedMessage.metadata,
            chatRoomId = roomId,
            senderId = senderId,
            senderName = sender.displayName ?: sender.username ?: "",
            sequenceNumber = savedMessage.sequenceNumber,
            timestamp = savedMessage.createdAt,
        )

        return SavedChatMessage(
            messageDto = messageToDto(savedMessage, sender),
            chatMessage = chatMessage,
            roomId = roomId,
            senderId = senderId,
            attachment = savedAttachment,
        )
    }

    @Transactional
    fun saveSystemMessage(
        chatRoom: ChatRoomJpaEntity,
        sender: UserJpaEntity,
        content: String,
    ): SavedChatMessage {
        val roomId = chatRoom.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다.")
        val sequenceNumber = messageSequenceService.getNextSequence(roomId)

        val message = MessageJpaEntity().apply {
            this.chatRoom = chatRoom
            this.sender = sender
            this.type = MessageType.SYSTEM
            this.content = content
            this.sequenceNumber = sequenceNumber
        }
        val savedMessage = messageJpaRepository.save(message)

        chatRoom.updatedAt = LocalDateTime.now()
        chatRoomJpaRepository.save(chatRoom)

        val senderId = sender.id ?: throw IllegalArgumentException("발신자 ID가 없습니다")
        val messageId = savedMessage.id ?: throw IllegalArgumentException("메시지 ID가 없습니다")

        val chatMessage = ChatMessage(
            id = messageId,
            content = savedMessage.content ?: "",
            messageType = savedMessage.type,
            metadata = savedMessage.metadata,
            chatRoomId = roomId,
            senderId = senderId,
            senderName = sender.displayName ?: sender.username ?: "",
            sequenceNumber = savedMessage.sequenceNumber,
            timestamp = savedMessage.createdAt,
        )

        return SavedChatMessage(
            messageDto = messageToDto(savedMessage, sender),
            chatMessage = chatMessage,
            roomId = roomId,
            senderId = senderId,
            attachment = null,
        )
    }

    private fun messageToDto(message: MessageJpaEntity, sender: UserJpaEntity): MessageDto =
        MessageDto(
            id = message.id ?: throw IllegalArgumentException("메시지 ID가 없습니다"),
            chatRoomId = message.chatRoom?.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다"),
            sender = com.kochat.adapter.inbound.web.chat.dto.ChatUserDto(
                id = sender.id ?: throw IllegalArgumentException("사용자 ID가 없습니다"),
                username = sender.username ?: "",
                displayName = sender.displayName,
                isActive = true,
                createdAt = sender.createdAt?.atZone(java.time.ZoneId.systemDefault())?.toLocalDateTime(),
            ),
            type = message.type,
            content = message.content,
            metadata = messageMetadataMapper.fromJson(message.metadata),
            sequenceNumber = message.sequenceNumber,
            isEdited = message.isEdited,
            isDeleted = message.isDeleted,
            createdAt = message.createdAt,
            editedAt = message.editedAt,
        )
}
