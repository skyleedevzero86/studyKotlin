package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.UserUseCase
import com.kominioai.domain.auth.application.port.out.*
import com.kominioai.domain.auth.domain.model.*
import com.kominioai.domain.auth.domain.service.AuthService
import com.kominioai.domain.auth.domain.service.UserService
import com.kominioai.global.exception.auth.AuthenticationException
import com.kominioai.global.exception.domain.DomainException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Service
class UserApplicationService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val loadAuthTokenPort: LoadAuthTokenPort,
    private val saveAuthTokenPort: SaveAuthTokenPort,
    private val authService: AuthService,
    private val userService: UserService
) : UserUseCase {

    override fun getProfile(userId: String): Mono<UserProfileResponse> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .map { user ->
                user?.let {
                    UserProfileResponse(
                        id = it.id.value,
                        email = it.email.value,
                        username = it.username.value,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        profileImageUrl = it.profileImageUrl,
                        phone = it.phone,
                        emailVerified = it.emailVerified,
                        twoFactorEnabled = it.twoFactorEnabled,
                        accountStatus = it.accountStatus.name,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        lastLoginAt = it.lastLoginAt
                    )
                } ?: throw AuthenticationException.InvalidCredentialsException()
            }
    }

    override fun updateProfile(userId: String, request: UserProfileRequest): Mono<UserProfileResponse> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    val updatedUser = it.updateProfile(
                        firstName = request.firstName,
                        lastName = request.lastName,
                        profileImageUrl = request.profileImageUrl,
                        phone = request.phone
                    )
                    saveUserPort.save(updatedUser)
                        .map { savedUser ->
                            savedUser?.let { saved ->
                                UserProfileResponse(
                                    id = saved.id.value,
                                    email = saved.email.value,
                                    username = saved.username.value,
                                    firstName = saved.firstName,
                                    lastName = saved.lastName,
                                    profileImageUrl = saved.profileImageUrl,
                                    phone = saved.phone,
                                    emailVerified = saved.emailVerified,
                                    twoFactorEnabled = saved.twoFactorEnabled,
                                    accountStatus = saved.accountStatus.name,
                                    createdAt = saved.createdAt,
                                    updatedAt = saved.updatedAt,
                                    lastLoginAt = saved.lastLoginAt
                                )
                            } ?: throw AuthenticationException.InvalidCredentialsException()
                        }
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun changePassword(userId: String, request: ChangePasswordRequest): Mono<Void> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (!authService.validateCredentials(it, request.currentPassword)) {
                        return@flatMap Mono.error<Void>(AuthenticationException.InvalidCredentialsException())
                    }
                    if (request.newPassword != request.confirmPassword) {
                        return@flatMap Mono.error<Void>(DomainException.ValidationException("Passwords do not match"))
                    }
                    if (!authService.validatePasswordStrength(request.newPassword)) {
                        return@flatMap Mono.error<Void>(DomainException.ValidationException("Password does not meet strength requirements"))
                    }
                    val updatedUser = it.updatePassword(authService.hashPassword(request.newPassword))
                    saveUserPort.save(updatedUser).then()
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun getActiveSessions(userId: String): Flux<UserSessionResponse> {
        return loadAuthTokenPort.loadActiveTokensByUserId(UserId(userId))
            .map { token ->
                UserSessionResponse(
                    sessionId = token.id.value,
                    deviceInfo = token.deviceInfo,
                    ipAddress = token.ipAddress,
                    userAgent = token.userAgent,
                    issuedAt = token.issuedAt,
                    expiresAt = token.expiresAt,
                    isCurrentSession = false
                )
            }
    }

    override fun revokeSession(userId: String, sessionId: String): Mono<Void> {
        return loadAuthTokenPort.loadById(AuthTokenId(sessionId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { token ->
                token?.let {
                    if (it.userId.value != userId) {
                        return@flatMap Mono.error<Void>(AuthenticationException.InvalidCredentialsException())
                    }
                    val revokedToken = it.revoke("User requested session revocation")
                    saveAuthTokenPort.save(revokedToken).then()
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun getSecurityLogs(userId: String, page: Int, size: Int): Mono<SecurityLogResponse> {
        return Mono.just(
            SecurityLogResponse(
                logs = emptyList(),
                totalCount = 0L,
                page = page,
                size = size
            )
        )
    }

    override fun deleteAccount(userId: String, request: DeleteAccountRequest): Mono<Void> {
        return loadUserPort.loadById(UserId(userId))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (!authService.validateCredentials(it, request.password)) {
                        return@flatMap Mono.error<Void>(AuthenticationException.InvalidCredentialsException())
                    }
                    if (request.confirmation != "DELETE") {
                        return@flatMap Mono.error<Void>(DomainException.ValidationException("Confirmation text must be 'DELETE'"))
                    }
                    val deletedUser = it.suspendAccount()
                    saveUserPort.save(deletedUser)
                        .then(saveAuthTokenPort.revokeAllUserTokens(it.id, "Account deletion"))
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }
}