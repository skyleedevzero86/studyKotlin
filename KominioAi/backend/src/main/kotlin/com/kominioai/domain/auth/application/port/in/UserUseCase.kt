package com.kominioai.domain.auth.application.port.`in`

import com.kominioai.domain.auth.application.dto.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface UserUseCase {
    fun getProfile(userId: String): Mono<UserProfileResponse>
    fun updateProfile(userId: String, request: UserProfileRequest): Mono<UserProfileResponse>
    fun changePassword(userId: String, request: ChangePasswordRequest): Mono<Void>
    fun getActiveSessions(userId: String): Flux<UserSessionResponse>
    fun revokeSession(userId: String, sessionId: String): Mono<Void>
    fun getSecurityLogs(userId: String, page: Int, size: Int): Mono<SecurityLogResponse>
    fun deleteAccount(userId: String, request: DeleteAccountRequest): Mono<Void>
}