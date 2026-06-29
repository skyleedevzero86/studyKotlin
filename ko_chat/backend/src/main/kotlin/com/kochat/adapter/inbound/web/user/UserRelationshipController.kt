package com.kochat.adapter.inbound.web.user

import com.kochat.adapter.inbound.web.user.dto.UserBlockHistoryDto
import com.kochat.adapter.inbound.web.user.dto.UserFriendRequestDto
import com.kochat.adapter.inbound.web.user.dto.UserRelationshipDto
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.application.user.UserRelationshipService
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "사용자 관계", description = "친구 요청, 친구 목록, 차단 목록 API")
@RestController
@RequestMapping("/api/v1/users")
class UserRelationshipController(
    private val chatUserResolver: ChatUserResolver,
    private val userRelationshipService: UserRelationshipService,
) {
    @Operation(summary = "내 친구 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/friends")
    fun getFriends(authentication: Authentication): ResponseEntity<List<UserRelationshipDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.getFriends(userId))
    }

    @Operation(summary = "친구 요청 보내기")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/friends/{targetUserId}")
    fun requestFriend(
        authentication: Authentication,
        @PathVariable targetUserId: Long,
    ): ResponseEntity<UserFriendRequestDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.requestFriend(userId, targetUserId))
    }

    @Operation(summary = "받은 친구 요청 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/friend-requests/incoming")
    fun getIncomingFriendRequests(authentication: Authentication): ResponseEntity<List<UserFriendRequestDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.getIncomingFriendRequests(userId))
    }

    @Operation(summary = "거부한 친구 요청 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/friend-requests/rejected")
    fun getRejectedFriendRequests(authentication: Authentication): ResponseEntity<List<UserFriendRequestDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.getRejectedFriendRequests(userId))
    }

    @Operation(summary = "친구 요청 수락")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/friend-requests/{requestId}/accept")
    fun acceptFriendRequest(
        authentication: Authentication,
        @PathVariable requestId: Long,
    ): ResponseEntity<UserFriendRequestDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.acceptFriendRequest(requestId, userId))
    }

    @Operation(summary = "친구 요청 거부")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/friend-requests/{requestId}/reject")
    fun rejectFriendRequest(
        authentication: Authentication,
        @PathVariable requestId: Long,
    ): ResponseEntity<UserFriendRequestDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.rejectFriendRequest(requestId, userId))
    }

    @Operation(summary = "친구 삭제")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @DeleteMapping("/friends/{targetUserId}")
    fun removeFriend(
        authentication: Authentication,
        @PathVariable targetUserId: Long,
    ): ResponseEntity<Void> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        userRelationshipService.removeFriend(userId, targetUserId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "내 차단 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/blocks")
    fun getBlocks(authentication: Authentication): ResponseEntity<List<UserRelationshipDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.getActiveBlocks(userId))
    }

    @Operation(summary = "내 차단 기록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/blocks/history")
    fun getBlockHistory(authentication: Authentication): ResponseEntity<List<UserBlockHistoryDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.getBlockHistory(userId))
    }

    @Operation(summary = "사용자 차단")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/blocks/{targetUserId}")
    fun blockUser(
        authentication: Authentication,
        @PathVariable targetUserId: Long,
    ): ResponseEntity<UserRelationshipDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(userRelationshipService.blockUser(userId, targetUserId))
    }

    @Operation(summary = "사용자 차단 해제")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @DeleteMapping("/blocks/{targetUserId}")
    fun unblockUser(
        authentication: Authentication,
        @PathVariable targetUserId: Long,
    ): ResponseEntity<Void> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        userRelationshipService.unblockUser(userId, targetUserId)
        return ResponseEntity.noContent().build()
    }
}
