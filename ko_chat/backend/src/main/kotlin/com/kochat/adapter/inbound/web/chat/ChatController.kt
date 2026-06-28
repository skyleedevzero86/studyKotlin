package com.kochat.adapter.inbound.web.chat

import com.kochat.adapter.inbound.web.chat.dto.ChatRoomDto
import com.kochat.adapter.inbound.web.chat.dto.ChatRoomMemberDto
import com.kochat.adapter.inbound.web.chat.dto.CreateChatRoomRequest
import com.kochat.adapter.inbound.web.chat.dto.CreateDirectChatRequest
import com.kochat.adapter.inbound.web.chat.dto.JoinChatRoomRequest
import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.inbound.web.chat.dto.MessagePageRequest
import com.kochat.adapter.inbound.web.chat.dto.MessagePageResponse
import com.kochat.domain.chat.model.MessageDirection
import com.kochat.domain.chat.service.ChatService
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "채팅", description = "채팅방 및 메시지 API")
@RestController
@RequestMapping("/api/v1/chat-rooms")
class ChatController(
    private val chatService: ChatService,
    private val chatUserResolver: ChatUserResolver,
) {
    @Operation(summary = "채팅방 생성")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping
    fun createChatRoom(
        authentication: Authentication,
        @Valid @RequestBody request: CreateChatRoomRequest,
    ): ResponseEntity<ChatRoomDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        val chatRoom = chatService.createChatRoom(request, userId)
        return ResponseEntity.ok(chatRoom)
    }

    @Operation(summary = "1:1 채팅방 찾기 또는 생성")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/direct")
    fun findOrCreateDirectRoom(
        authentication: Authentication,
        @Valid @RequestBody request: CreateDirectChatRequest,
    ): ResponseEntity<ChatRoomDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        val chatRoom = chatService.findOrCreateDirectRoom(request.targetUserId, userId)
        return ResponseEntity.ok(chatRoom)
    }

    @Operation(summary = "채팅방 조회")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{id}")
    fun getChatRoom(
        authentication: Authentication,
        @PathVariable id: Long,
    ): ResponseEntity<ChatRoomDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(chatService.getChatRoom(id, userId))
    }

    @Operation(summary = "내 채팅방 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping
    fun getChatRooms(
        authentication: Authentication,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<Page<ChatRoomDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(chatService.getChatRooms(userId, pageable))
    }

    @Operation(summary = "채팅방 참여")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{id}/members")
    fun joinChatRoom(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestBody request: JoinChatRoomRequest,
    ): ResponseEntity<Void> {
        val userId = request.userId ?: chatUserResolver.resolveUserId(authentication.name)
        chatService.joinChatRoom(id, userId)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "채팅방 나가기")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @DeleteMapping("/{id}/members/me")
    fun leaveChatRoom(
        authentication: Authentication,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        chatService.leaveChatRoom(id, userId)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "채팅방 멤버 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{id}/members")
    fun getChatRoomMembers(@PathVariable id: Long): ResponseEntity<List<ChatRoomMemberDto>> =
        ResponseEntity.ok(chatService.getChatRoomMembers(id))

    @Operation(summary = "채팅방 읽음 처리")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{id}/read")
    fun markRoomAsRead(
        authentication: Authentication,
        @PathVariable id: Long,
    ): ResponseEntity<ChatRoomDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(chatService.markRoomAsRead(id, userId))
    }

    @Operation(summary = "메시지 목록 (페이지)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{id}/messages")
    fun getMessages(
        authentication: Authentication,
        @PathVariable id: Long,
        @PageableDefault(size = 50) pageable: Pageable,
    ): ResponseEntity<Page<MessageDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(chatService.getMessages(id, userId, pageable))
    }

    @Operation(summary = "메시지 목록 (커서)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{id}/messages/cursor")
    fun getMessagesByCursor(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "BEFORE") direction: MessageDirection,
    ): ResponseEntity<MessagePageResponse> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        val request = MessagePageRequest(
            chatRoomId = id,
            cursor = cursor,
            limit = limit.coerceAtMost(100),
            direction = direction,
        )
        return ResponseEntity.ok(chatService.getMessagesByCursor(request, userId))
    }

    @Operation(summary = "채팅방 검색")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/search")
    fun searchChatRooms(
        authentication: Authentication,
        @RequestParam(required = false, defaultValue = "") q: String,
    ): ResponseEntity<List<ChatRoomDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(chatService.searchChatRooms(q, userId))
    }
}
