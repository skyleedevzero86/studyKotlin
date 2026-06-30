package com.kochat.global.application.chat

import com.kochat.adapter.inbound.web.chat.dto.AttachmentUploadResponse
import com.kochat.adapter.inbound.web.chat.dto.MessageMetadataDto
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import com.kochat.adapter.outbound.storage.MinioStorageService
import com.kochat.domain.chat.model.MessageType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class ChatAttachmentService(
    private val minioStorageService: MinioStorageService,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
) {
    fun upload(chatRoomId: Long, userId: Long, file: MultipartFile): AttachmentUploadResponse {
        requireMember(chatRoomId, userId)
        require(!file.isEmpty) { "빈 파일은 업로드할 수 없습니다." }
        require(file.size <= MAX_FILE_SIZE) { "파일 크기는 50MB 이하여야 합니다." }

        val stored = minioStorageService.uploadChatFile(chatRoomId, file)
        val expiresAt = LocalDateTime.now().plusDays(FILE_RETENTION_DAYS)
        val messageType = if (stored.mimeType.startsWith("image/")) MessageType.IMAGE else MessageType.FILE

        val metadata = MessageMetadataDto(
            objectKey = stored.objectKey,
            url = stored.url,
            fileName = stored.fileName,
            mimeType = stored.mimeType,
            size = stored.size,
            expiresAt = expiresAt,
        )

        return AttachmentUploadResponse(
            messageType = messageType,
            metadata = metadata,
            content = if (messageType == MessageType.IMAGE) null else stored.fileName,
        )
    }

    fun refreshDownloadUrl(objectKey: String, userId: Long): String {
        val roomId = extractRoomId(objectKey)
        chatRoomMemberJpaRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)
            ?: throw IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다.")
        return minioStorageService.createPresignedUrl(objectKey)
    }

    private fun requireMember(chatRoomId: Long, userId: Long) {
        chatRoomMemberJpaRepository.findByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId, userId)
            ?: throw IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다.")
    }

    private fun extractRoomId(objectKey: String): Long {
        val parts = objectKey.split("/")
        require(parts.size >= 2 && parts[0] == "chat") { "잘못된 파일 경로입니다." }
        return parts[1].toLongOrNull() ?: throw IllegalArgumentException("잘못된 파일 경로입니다.")
    }

    companion object {
        private const val MAX_FILE_SIZE = 50L * 1024 * 1024
        private const val FILE_RETENTION_DAYS = 90L
    }
}
