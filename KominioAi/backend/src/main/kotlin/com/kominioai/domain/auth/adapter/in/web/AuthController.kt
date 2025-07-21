package com.kominioai.domain.auth.adapter.`in`.web

import com.kominioai.domain.auth.application.dto.*
import com.kominioai.domain.auth.application.port.`in`.AuthUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authUseCase: AuthUseCase
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): Mono<ResponseEntity<LoginResponse>> =
        authUseCase.login(request)
            .map { ResponseEntity.ok(it) }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(LoginResponse(
                    accessToken = "",
                    refreshToken = "",
                    tokenType = "",
                    expiresIn = 0,
                    user = null,
                    requiresTwoFactor = false
                )))
            }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): Mono<ResponseEntity<RegisterResponse>> =
        authUseCase.register(request)
            .map { ResponseEntity.ok(it) }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(RegisterResponse(
                    userId = "",
                    email = "",
                    username = "",
                    message = error.message ?: "Registration failed",
                    requiresEmailVerification = false,
                    registeredAt = java.time.LocalDateTime.now()
                )))
            }

    @PostMapping("/logout")
    fun logout(@RequestHeader("X-USER-ID") userId: String): Mono<ResponseEntity<Void>> =
        authUseCase.logout(userId)
            .thenReturn(ResponseEntity.ok().build())

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): Mono<ResponseEntity<RefreshTokenResponse>> =
        authUseCase.refreshToken(request)
            .map { ResponseEntity.ok(it) }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(RefreshTokenResponse(
                    accessToken = "",
                    refreshToken = "",
                    tokenType = "",
                    expiresIn = 0
                )))
            }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): Mono<ResponseEntity<Void>> =
        authUseCase.forgotPassword(request)
            .thenReturn(ResponseEntity.ok().build())

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): Mono<ResponseEntity<Void>> =
        authUseCase.resetPassword(request)
            .thenReturn(ResponseEntity.ok().build())

    @PostMapping("/verify-email")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): Mono<ResponseEntity<Void>> =
        authUseCase.verifyEmail(request)
            .thenReturn(ResponseEntity.ok().build())

    @PostMapping("/resend-verification")
    fun resendVerification(@Valid @RequestBody request: ResendVerificationRequest): Mono<ResponseEntity<Void>> =
        authUseCase.resendVerification(request)
            .thenReturn(ResponseEntity.ok().build())

    @PostMapping("/social/login")
    fun socialLogin(@Valid @RequestBody request: SocialLoginRequest): Mono<ResponseEntity<SocialLoginResponse>> =
        authUseCase.socialLogin(request)
            .map { ResponseEntity.ok(it) }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.badRequest().body(SocialLoginResponse(
                    accessToken = "",
                    refreshToken = "",
                    tokenType = "",
                    expiresIn = 0,
                    user = null,
                    isNewUser = false,
                    socialAccountInfo = null
                )))
            }
}