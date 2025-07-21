package com.kominioai.domain.auth.adapter.`in`.web

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.UserUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userUseCase: UserUseCase
) {
    @GetMapping("/me")
    fun getProfile(@RequestHeader("X-USER-ID") userId: String): Mono<ResponseEntity<UserProfileResponse>> =
        userUseCase.getProfile(userId)
            .map { ResponseEntity.ok(it) }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(UserProfileResponse(
                    id = "",
                    email = "",
                    username = "",
                    firstName = null,
                    lastName = null,
                    profileImageUrl = null,
                    phone = null,
                    emailVerified = false,
                    twoFactorEnabled = false,
                    accountStatus = "",
                    createdAt = java.time.LocalDateTime.now(),
                    updatedAt = java.time.LocalDateTime.now(),
                    lastLoginAt = null
                )))
            }

    @PutMapping("/me")
    fun updateProfile(@RequestHeader("X-USER-ID") userId: String, @Valid @RequestBody request: UserProfileRequest): Mono<ResponseEntity<UserProfileResponse>> =
        userUseCase.updateProfile(userId, request)
            .map { ResponseEntity.ok(it) }

    @PutMapping("/me/password")
    fun changePassword(@RequestHeader("X-USER-ID") userId: String, @Valid @RequestBody request: ChangePasswordRequest): Mono<ResponseEntity<Void>> =
        userUseCase.changePassword(userId, request)
            .thenReturn(ResponseEntity.ok().build())

    @GetMapping("/me/sessions")
    fun getActiveSessions(@RequestHeader("X-USER-ID") userId: String): Flux<UserSessionResponse> =
        userUseCase.getActiveSessions(userId)

    @DeleteMapping("/me/sessions/{sessionId}")
    fun revokeSession(@RequestHeader("X-USER-ID") userId: String, @PathVariable sessionId: String): Mono<ResponseEntity<Void>> =
        userUseCase.revokeSession(userId, sessionId)
            .thenReturn(ResponseEntity.ok().build())

    @GetMapping("/me/security-log")
    fun getSecurityLogs(@RequestHeader("X-USER-ID") userId: String, @RequestParam page: Int, @RequestParam size: Int): Mono<ResponseEntity<SecurityLogResponse>> =
        userUseCase.getSecurityLogs(userId, page, size)
            .map { ResponseEntity.ok(it) }

    @DeleteMapping("/me")
    fun deleteAccount(@RequestHeader("X-USER-ID") userId: String, @Valid @RequestBody request: DeleteAccountRequest): Mono<ResponseEntity<Void>> =
        userUseCase.deleteAccount(userId, request)
            .thenReturn(ResponseEntity.ok().build())
}