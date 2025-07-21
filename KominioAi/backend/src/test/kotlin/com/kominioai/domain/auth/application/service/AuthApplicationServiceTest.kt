package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.application.dto.LoginRequest
import com.kominioai.domain.auth.application.dto.RegisterRequest
import com.kominioai.domain.auth.application.port.out.*
import com.kominioai.domain.auth.domain.model.*
import com.kominioai.domain.auth.domain.service.AuthService
import com.kominioai.domain.auth.domain.service.UserService
import com.kominioai.global.exception.auth.AuthenticationException
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class AuthApplicationServiceTest : StringSpec({
    val loadUserPort = mockk<LoadUserPort>()
    val saveUserPort = mockk<SaveUserPort>()
    val loadAuthTokenPort = mockk<LoadAuthTokenPort>()
    val saveAuthTokenPort = mockk<SaveAuthTokenPort>()
    val cachePort = mockk<CachePort>()
    val emailPort = mockk<EmailPort>()
    val socialAuthPort = mockk<SocialAuthPort>()
    val authService = mockk<AuthService>()
    val userService = mockk<UserService>()
    val passwordEncoder = mockk<org.springframework.security.crypto.password.PasswordEncoder>()
    val jwtService = mockk<JwtService>()
    val eventPublisher = mockk<org.springframework.context.ApplicationEventPublisher>(relaxed = true)

    val service = AuthApplicationService(
        loadUserPort, saveUserPort, loadAuthTokenPort, saveAuthTokenPort, cachePort, emailPort, socialAuthPort,
        authService, userService, passwordEncoder, jwtService, eventPublisher
    )

    "login should fail if user not found" {
        every { loadUserPort.loadByEmailOrUsername(any()) } returns Mono.empty()

        StepVerifier.create(service.login(LoginRequest("notfound", "pw")))
            .expectError(AuthenticationException.InvalidCredentialsException::class.java)
            .verify()
    }

    "login should fail if password is invalid" {
        val user = User.create(
            email = Email("test@example.com"),
            passwordHash = PasswordHash("hashed"),
            username = Username("testuser")
        )

        every { loadUserPort.loadByEmailOrUsername(any()) } returns Mono.just(user)
        every { userService.canUserLogin(any()) } returns true
        every { authService.validateCredentials(any(), any()) } returns false

        StepVerifier.create(service.login(LoginRequest("test@example.com", "wrongpassword")))
            .expectError(AuthenticationException.InvalidCredentialsException::class.java)
            .verify()
    }

    "login should succeed with valid credentials" {
        val user = User.create(
            email = Email("test@example.com"),
            passwordHash = PasswordHash("hashed"),
            username = Username("testuser")
        )

        every { loadUserPort.loadByEmailOrUsername(any()) } returns Mono.just(user)
        every { userService.canUserLogin(any()) } returns true
        every { authService.validateCredentials(any(), any()) } returns true
        every { saveUserPort.save(any()) } returns Mono.just(user)
        every { saveAuthTokenPort.save(any()) } returns Mono.just(mockk<AuthToken>())
        every { jwtService.generateAccessToken(any()) } returns "access-token"
        every { jwtService.generateRefreshToken(any()) } returns "refresh-token"
        every { jwtService.getAccessTokenExpirySeconds() } returns 3600L

        StepVerifier.create(service.login(LoginRequest("test@example.com", "password")))
            .expectNextMatches { response ->
                response.accessToken == "access-token" &&
                        response.refreshToken == "refresh-token" &&
                        response.user.email == "test@example.com"
            }
            .verifyComplete()
    }

    "register should fail if email already exists" {
        every { loadUserPort.existsByEmail(any()) } returns Mono.just(true)
        every { loadUserPort.existsByUsername(any()) } returns Mono.just(false)

        val request = RegisterRequest(
            email = "existing@example.com",
            username = "newuser",
            password = "password123",
            confirmPassword = "password123"
        )

        StepVerifier.create(service.register(request))
            .expectError()
            .verify()
    }

    "register should succeed with valid data" {
        every { loadUserPort.existsByEmail(any()) } returns Mono.just(false)
        every { loadUserPort.existsByUsername(any()) } returns Mono.just(false)
        every { authService.validatePasswordStrength(any()) } returns true
        every { authService.hashPassword(any()) } returns PasswordHash("hashed")
        every { authService.generateEmailVerificationToken() } returns "verification-token"
        every { authService.calculateTokenExpiration(any()) } returns LocalDateTime.now().plusHours(1)
        every { saveUserPort.save(any()) } returns Mono.just(mockk<User>())
        every { emailPort.sendVerificationEmail(any(), any(), any()) } returns Mono.empty()

        val request = RegisterRequest(
            email = "new@example.com",
            username = "newuser",
            password = "password123",
            confirmPassword = "password123"
        )

        StepVerifier.create(service.register(request))
            .expectNextMatches { response ->
                response.email == "new@example.com" &&
                        response.username == "newuser" &&
                        response.requiresEmailVerification
            }
            .verifyComplete()
    }
})