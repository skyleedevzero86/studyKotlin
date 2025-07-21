package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.AuthUseCase
import com.kominioai.domain.auth.application.port.out.*
import com.kominioai.domain.auth.domain.model.*
import com.kominioai.domain.auth.domain.service.AuthService
import com.kominioai.domain.auth.domain.service.UserService
import com.kominioai.domain.auth.domain.event.*
import com.kominioai.global.exception.auth.AuthenticationException
import com.kominioai.global.exception.auth.AuthorizationException
import com.kominioai.global.exception.domain.DomainException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class AuthApplicationService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val loadAuthTokenPort: LoadAuthTokenPort,
    private val saveAuthTokenPort: SaveAuthTokenPort,
    private val cachePort: CachePort,
    private val emailPort: EmailPort,
    private val socialAuthPort: SocialAuthPort,
    private val authService: AuthService,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val eventPublisher: ApplicationEventPublisher
) : AuthUseCase {

    override fun login(request: LoginRequest): Mono<LoginResponse> {
        return loadUserPort.loadByEmailOrUsername(request.emailOrUsername)
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (!userService.canUserLogin(it)) {
                        return@flatMap handleFailedLogin(it, request, "Account not active or locked")
                    }

                    if (!authService.validateCredentials(it, request.password)) {
                        return@flatMap handleFailedLogin(it, request, "Invalid credentials")
                    }

                    if (it.twoFactorEnabled && request.twoFactorCode.isNullOrBlank()) {
                        return@flatMap Mono.just(LoginResponse(
                            accessToken = "",
                            refreshToken = "",
                            tokenType = "Bearer",
                            expiresIn = 0,
                            user = mapToUserInfo(it),
                            requiresTwoFactor = true
                        ))
                    }

                    if (it.twoFactorEnabled && !request.twoFactorCode.isNullOrBlank()) {
                        return@flatMap validateTwoFactorAndLogin(it, request)
                    }

                    handleSuccessfulLogin(it, request)
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
            .doOnError { error ->
                if (error !is AuthenticationException) {
                    eventPublisher.publishEvent(AuthFailedEvent.create(
                        email = request.emailOrUsername,
                        username = null,
                        failureReason = error.message ?: "Unknown error",
                        ipAddress = null,
                        userAgent = null
                    ))
                }
            }
    }

    override fun register(request: RegisterRequest): Mono<RegisterResponse> {
        if (request.password != request.confirmPassword) {
            return Mono.error(DomainException.ValidationException("Passwords do not match"))
        }

        if (!authService.validatePasswordStrength(request.password)) {
            return Mono.error(DomainException.ValidationException("Password does not meet strength requirements"))
        }

        return Mono.zip(
            loadUserPort.existsByEmail(Email(request.email)),
            loadUserPort.existsByUsername(Username(request.username))
        ).flatMap { tuple ->
            val emailExists = tuple.t1
            val usernameExists = tuple.t2

            when {
                emailExists -> Mono.error(DomainException.ValidationException("Email already exists"))
                usernameExists -> Mono.error(DomainException.ValidationException("Username already exists"))
                else -> {
                    val user = User.create(
                        email = Email(request.email),
                        passwordHash = authService.hashPassword(request.password),
                        username = Username(request.username),
                        firstName = request.firstName,
                        lastName = request.lastName,
                        phone = request.phone
                    )

                    saveUserPort.save(user)
                        .flatMap { savedUser ->
                            savedUser?.let { it ->
                                val verificationToken = authService.generateEmailVerificationToken()
                                val tokenExpiry = authService.calculateTokenExpiration(30)

                                val userWithToken = it.setEmailVerificationToken(verificationToken, tokenExpiry)
                                saveUserPort.save(userWithToken)
                                    .flatMap { finalUser ->
                                        finalUser?.let { final ->
                                            // 먼저 이벤트 발행
                                            eventPublisher.publishEvent(UserRegisteredEvent.from(final))

                                            emailPort.sendVerificationEmail(
                                                final.email.value,
                                                verificationToken,
                                                final.username.value
                                            ).then(
                                                Mono.just(RegisterResponse(
                                                    userId = final.id.value,
                                                    email = final.email.value,
                                                    username = final.username.value,
                                                    message = "Registration successful. Please check your email for verification.",
                                                    requiresEmailVerification = true,
                                                    registeredAt = final.createdAt
                                                ))
                                            )
                                        } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
                                    }
                            } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
                        }
                }
            }
        }
    }

    override fun logout(userId: String): Mono<Void> {
        return saveAuthTokenPort.revokeAllUserTokens(UserId(userId), "User logout")
            .flatMap { cachePort.delete("session:$userId") }
            .then()
    }

    override fun refreshToken(request: RefreshTokenRequest): Mono<RefreshTokenResponse> {
        return loadAuthTokenPort.loadByRefreshToken(request.refreshToken)
            .switchIfEmpty(Mono.error(AuthenticationException.TokenExpiredException()))
            .flatMap { token ->
                token?.let {
                    if (it.isExpired() || it.isRevoked()) {
                        return@flatMap Mono.error<RefreshTokenResponse>(AuthenticationException.TokenExpiredException())
                    }
                    loadUserPort.loadById(it.userId)
                        .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
                        .flatMap { user ->
                            user?.let { u ->
                                val newAccessToken = jwtService.generateAccessToken(u)
                                val newRefreshToken = jwtService.generateRefreshToken(u)
                                val expiresIn = jwtService.getAccessTokenExpirySeconds()
                                val updatedToken = it.copy(
                                    accessToken = newAccessToken,
                                    refreshToken = newRefreshToken,
                                    expiresAt = LocalDateTime.now().plusSeconds(expiresIn.toLong())
                                )
                                saveAuthTokenPort.save(updatedToken)
                                    .thenReturn(
                                        RefreshTokenResponse(
                                            accessToken = newAccessToken,
                                            refreshToken = newRefreshToken,
                                            tokenType = "Bearer",
                                            expiresIn = expiresIn
                                        )
                                    )
                            } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
                        }
                } ?: Mono.error(AuthenticationException.TokenExpiredException())
            }
    }

    override fun forgotPassword(request: ForgotPasswordRequest): Mono<Void> {
        return loadUserPort.loadByEmail(Email(request.email))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    val token = authService.generatePasswordResetToken()
                    val expiresAt = authService.calculateTokenExpiration(30)
                    val updatedUser = it.setPasswordResetToken(token, expiresAt)
                    saveUserPort.save(updatedUser)
                        .then(emailPort.sendPasswordResetEmail(it.email.value, token, it.username.value))
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun resetPassword(request: ResetPasswordRequest): Mono<Void> {
        if (request.newPassword != request.confirmPassword) {
            return Mono.error(DomainException.ValidationException("Passwords do not match"))
        }
        if (!authService.validatePasswordStrength(request.newPassword)) {
            return Mono.error(DomainException.ValidationException("Password does not meet strength requirements"))
        }
        return loadUserPort.loadByEmailOrUsername(request.token)
            .switchIfEmpty(Mono.error(AuthenticationException.TokenExpiredException()))
            .flatMap { user ->
                user?.let {
                    if (it.passwordResetToken != request.token || it.passwordResetExpiresAt == null || authService.isTokenExpired(it.passwordResetExpiresAt)) {
                        return@flatMap Mono.error<Void>(AuthenticationException.TokenExpiredException())
                    }
                    val updatedUser = it.updatePassword(authService.hashPassword(request.newPassword)).clearPasswordResetToken()
                    saveUserPort.save(updatedUser).then()
                } ?: Mono.error(AuthenticationException.TokenExpiredException())
            }
    }

    override fun verifyEmail(request: VerifyEmailRequest): Mono<Void> {
        return loadUserPort.loadByEmailOrUsername(request.token)
            .switchIfEmpty(Mono.error(AuthenticationException.TokenExpiredException()))
            .flatMap { user ->
                user?.let {
                    if (it.emailVerificationToken != request.token || it.emailVerificationExpiresAt == null || authService.isTokenExpired(it.emailVerificationExpiresAt)) {
                        return@flatMap Mono.error<Void>(AuthenticationException.TokenExpiredException())
                    }
                    val updatedUser = it.verifyEmail()
                    saveUserPort.save(updatedUser).then()
                } ?: Mono.error(AuthenticationException.TokenExpiredException())
            }
    }

    override fun resendVerification(request: ResendVerificationRequest): Mono<Void> {
        return loadUserPort.loadByEmail(Email(request.email))
            .switchIfEmpty(Mono.error(AuthenticationException.InvalidCredentialsException()))
            .flatMap { user ->
                user?.let {
                    if (it.emailVerified) {
                        return@flatMap Mono.empty<Void>()
                    }
                    val token = authService.generateEmailVerificationToken()
                    val expiresAt = authService.calculateTokenExpiration(30)
                    val updatedUser = it.setEmailVerificationToken(token, expiresAt)
                    saveUserPort.save(updatedUser)
                        .then(emailPort.sendVerificationEmail(it.email.value, token, it.username.value))
                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
            }
    }

    override fun socialLogin(request: SocialLoginRequest): Mono<SocialLoginResponse> {
        val provider = request.provider.uppercase()
        val userInfoMono = when (provider) {
            "GOOGLE" -> socialAuthPort.getGoogleUserInfo(request.accessToken)
            "KAKAO" -> socialAuthPort.getKakaoUserInfo(request.accessToken)
            "NAVER" -> socialAuthPort.getNaverUserInfo(request.accessToken)
            else -> Mono.error(AuthenticationException.InvalidCredentialsException())
        }
        return userInfoMono.flatMap { socialUserInfo ->
            loadUserPort.loadByEmail(Email(socialUserInfo.email ?: ""))
                .flatMap { user ->
                    user?.let {
                        handleSuccessfulLogin(it, LoginRequest(it.email.value, "", false, null, request.deviceInfo))
                            .map { loginResponse ->
                                SocialLoginResponse(
                                    accessToken = loginResponse.accessToken,
                                    refreshToken = loginResponse.refreshToken,
                                    tokenType = loginResponse.tokenType,
                                    expiresIn = loginResponse.expiresIn,
                                    user = loginResponse.user,
                                    isNewUser = false,
                                    socialAccountInfo = SocialAccountInfo(
                                        provider = provider,
                                        providerUserId = socialUserInfo.providerUserId,
                                        displayName = socialUserInfo.displayName,
                                        profileImageUrl = socialUserInfo.profileImageUrl
                                    )
                                )
                            }
                    } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
                }
                .switchIfEmpty(
                    Mono.defer {
                        val newUser = User.create(
                            email = Email(socialUserInfo.email ?: ""),
                            passwordHash = authService.hashPassword(java.util.UUID.randomUUID().toString()),
                            username = Username(socialUserInfo.providerUserId),
                            firstName = socialUserInfo.firstName,
                            lastName = socialUserInfo.lastName
                        ).verifyEmail()
                        saveUserPort.save(newUser)
                            .flatMap { savedUser ->
                                savedUser?.let {
                                    handleSuccessfulLogin(it, LoginRequest(it.email.value, "", false, null, request.deviceInfo))
                                        .map { loginResponse ->
                                            SocialLoginResponse(
                                                accessToken = loginResponse.accessToken,
                                                refreshToken = loginResponse.refreshToken,
                                                tokenType = loginResponse.tokenType,
                                                expiresIn = loginResponse.expiresIn,
                                                user = loginResponse.user,
                                                isNewUser = true,
                                                socialAccountInfo = SocialAccountInfo(
                                                    provider = provider,
                                                    providerUserId = socialUserInfo.providerUserId,
                                                    displayName = socialUserInfo.displayName,
                                                    profileImageUrl = socialUserInfo.profileImageUrl
                                                )
                                            )
                                        }
                                } ?: Mono.error(AuthenticationException.InvalidCredentialsException())
                            }
                    }
                )
        }
    }

    private fun handleFailedLogin(user: User, request: LoginRequest, reason: String): Mono<LoginResponse> {
        val updatedUser = user.incrementFailedLoginAttempts()
        return saveUserPort.save(updatedUser)
            .then(Mono.error<LoginResponse>(AuthenticationException.InvalidCredentialsException()))
            .doOnError {
                eventPublisher.publishEvent(AuthFailedEvent.create(
                    email = user.email.value,
                    username = user.username.value,
                    failureReason = reason,
                    ipAddress = null,
                    userAgent = null
                ))
            }
    }

    private fun handleSuccessfulLogin(user: User, request: LoginRequest): Mono<LoginResponse> {
        val updatedUser = user.resetFailedLoginAttempts().updateLastLogin()
        val accessToken = jwtService.generateAccessToken(updatedUser)
        val refreshToken = jwtService.generateRefreshToken(updatedUser)
        val expiresIn = jwtService.getAccessTokenExpirySeconds()
        val authToken = AuthToken.create(
            userId = updatedUser.id,
            tokenType = TokenType.ACCESS,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = LocalDateTime.now().plusSeconds(expiresIn.toLong()),
            deviceInfo = request.deviceInfo
        )
        return saveUserPort.save(updatedUser)
            .then(saveAuthTokenPort.save(authToken))
            .thenReturn(
                LoginResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    tokenType = "Bearer",
                    expiresIn = expiresIn,
                    user = mapToUserInfo(updatedUser),
                    requiresTwoFactor = false
                )
            )
            .doOnSuccess {
                eventPublisher.publishEvent(UserLoggedInEvent.from(updatedUser, null, null, request.deviceInfo))
            }
    }

    private fun validateTwoFactorAndLogin(user: User, request: LoginRequest): Mono<LoginResponse> {
        // 2FA 검증 로직 (예: OTP 코드 검증)
        // 생략: 실제 구현에서는 TwoFactorAuthService를 활용
        return handleSuccessfulLogin(user, request)
    }

    private fun mapToUserInfo(user: User): UserInfo {
        return UserInfo(
            id = user.id.value,
            email = user.email.value,
            username = user.username.value,
            firstName = user.firstName,
            lastName = user.lastName,
            profileImageUrl = user.profileImageUrl,
            emailVerified = user.emailVerified,
            twoFactorEnabled = user.twoFactorEnabled,
            lastLoginAt = user.lastLoginAt
        )
    }
}