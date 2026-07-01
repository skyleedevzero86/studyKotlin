package com.kochat.adapter.inbound.web.admin

import com.kochat.adapter.inbound.web.chat.dto.ChatRoomDto
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomMemberDto
import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.outbound.persistence.chat.MessageJpaEntity
import com.kochat.adapter.outbound.persistence.chat.MessageJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.chat.service.ChatService
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId

@Tag(name = "관리자 채팅", description = "관리자용 채팅방·멤버·메시지 관리 API")
@RestController
@RequestMapping("/api/v1/admin/chat-rooms")
class AdminChatController(
    private val chatService: ChatService,
    private val messageJpaRepository: MessageJpaRepository,
    private val chatUserResolver: ChatUserResolver,
) {
    @Operation(summary = "전체 활성 채팅방 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("", "/")
    fun listChatRooms(
        authentication: Authentication,
        @PageableDefault(size = 30) pageable: Pageable,
    ): ResponseEntity<Page<ChatRoomDto>> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(chatService.getAllChatRoomsForAdmin(adminUserId, pageable))
    }

    @Operation(summary = "채팅방 멤버 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{id}/members")
    fun getMembers(@PathVariable id: Long): ResponseEntity<List<ChatRoomMemberDto>> =
        ResponseEntity.ok(chatService.getChatRoomMembers(id))

    @Operation(summary = "관리자 강제 퇴장")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{id}/members/{targetUserId}/kick")
    fun kickMember(
        authentication: Authentication,
        @PathVariable id: Long,
        @PathVariable targetUserId: Long,
    ): ResponseEntity<Void> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        chatService.adminKickMember(id, targetUserId, adminUserId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "관리자 메시지 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{id}/messages")
    fun getMessages(
        authentication: Authentication,
        @PathVariable id: Long,
        @PageableDefault(size = 50) pageable: Pageable,
    ): ResponseEntity<Page<MessageDto>> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        val messages = messageJpaRepository.findByChatRoomIdVisibleTo(
            chatRoomId = id,
            viewerUserId = adminUserId,
            viewerIsAdmin = true,
            pageable = pageable,
        ).map { messageToDto(it) }
        return ResponseEntity.ok(messages)
    }

    private fun messageToDto(message: MessageJpaEntity): MessageDto {
        val sender = message.sender ?: throw IllegalArgumentException("메시지 발신자가 없습니다.")
        val chatRoom = message.chatRoom ?: throw IllegalArgumentException("채팅방이 없습니다.")
        return MessageDto(
            id = message.id ?: throw IllegalArgumentException("메시지 ID가 없습니다."),
            chatRoomId = chatRoom.id ?: throw IllegalArgumentException("채팅방 ID가 없습니다."),
            sender = userToDto(sender),
            type = message.type,
            content = message.content,
            isEdited = message.isEdited,
            isDeleted = message.isDeleted,
            createdAt = message.createdAt,
            editedAt = message.editedAt,
            sequenceNumber = message.sequenceNumber,
        )
    }

    private fun userToDto(user: UserJpaEntity): ChatUserDto {
        val createdAt = user.createdAt?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        return ChatUserDto(
            id = user.id ?: throw IllegalArgumentException("사용자 ID가 없습니다."),
            username = user.username ?: "",
            displayName = user.displayName,
            isActive = true,
            createdAt = createdAt,
        )
    }
}
