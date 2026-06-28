package com.kochat.adapter.inbound.web.user

import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.domain.user.model.UserStatus
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId

@Tag(name = "사용자 검색", description = "1:1 채팅 상대 검색 API")
@RestController
@RequestMapping("/api/v1/users")
class UserSearchController(
    private val userJpaRepository: UserJpaRepository,
    private val chatUserResolver: ChatUserResolver,
) {
    @Operation(summary = "사용자 검색", description = "1:1 채팅을 시작할 상대를 검색합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/search")
    fun searchUsers(
        authentication: Authentication,
        @RequestParam(defaultValue = "") q: String,
        @RequestParam(defaultValue = "20") limit: Int,
    ): List<ChatUserDto> {
        val currentUserId = chatUserResolver.resolveUserId(authentication.name)
        val pageable = PageRequest.of(0, limit.coerceIn(1, 50))
        val users = if (q.isBlank()) {
            userJpaRepository.findSearchableActiveUsers(currentUserId, pageable)
        } else {
            userJpaRepository.searchActiveUsers(currentUserId, q.trim(), pageable)
        }
        return users.content.map { userToDto(it) }
    }

    private fun userToDto(user: UserJpaEntity): ChatUserDto {
        val createdAt = user.createdAt?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        return ChatUserDto(
            id = user.id ?: throw IllegalArgumentException("사용자 ID가 없습니다"),
            username = user.username ?: "",
            displayName = user.displayName,
            isActive = user.status == UserStatus.ACTIVE,
            createdAt = createdAt,
        )
    }
}
