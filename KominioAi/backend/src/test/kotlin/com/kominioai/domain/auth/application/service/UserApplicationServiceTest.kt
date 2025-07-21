package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.application.dto.UserProfileRequest
import com.kominioai.domain.auth.application.dto.ChangePasswordRequest
import com.kominioai.domain.auth.application.port.out.*
import com.kominioai.domain.auth.domain.model.*
import com.kominioai.domain.auth.domain.service.AuthService
import com.kominioai.domain.auth.domain.service.UserService
import com.kominioai.global.exception.auth.AuthenticationException
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class UserApplicationServiceTest : StringSpec({
    val loadUserPort = mockk<LoadUserPort>()
    val saveUserPort = mockk<SaveUserPort>()
    val loadAuthTokenPort = mockk<LoadAuthTokenPort>()
    val saveAuthTokenPort = mockk<SaveAuthTokenPort>()
    val authService = mockk<AuthService>()
    val userService = mockk<UserService>()

    val service = UserApplicationService(
        loadUserPort, saveUserPort, loadAuthTokenPort, saveAuthTokenPort, authService, userService
    )

    "getProfile should return user profile" {
        val user = User.create(
            email = Email("test@example.com"),
            passwordHash = PasswordHash("hashed"),
            username = Username("testuser")
        )

        every { loadUserPort.loadById(any()) } returns Mono.just(user)

        StepVerifier.create(service.getProfile("user-id"))
            .expectNextMatches { response ->
                response.email == "test@example.com" &&
                        response.username == "testuser"
            }
            .verifyComplete()
    }

    "updateProfile should update user profile" {
        val user = User.create(
            email = Email("test@example.com"),
            passwordHash = PasswordHash("hashed"),
            username = Username("testuser")
        )

        every { loadUserPort.loadById(any()) } returns Mono.just(user)
        every { saveUserPort.save(any()) } returns Mono.just(user)

        val request = UserProfileRequest(
            firstName = "John",
            lastName = "Doe",
            profileImageUrl = "http://example.com/image.jpg",
            phone = "1234567890"
        )

        StepVerifier.create(service.updateProfile("user-id", request))
            .expectNextMatches { response ->
                response.firstName == "John" &&
                        response.lastName == "Doe"
            }
            .verifyComplete()
    }

    "changePassword should fail if current password is wrong" {
        val user = User.create(
            email = Email("test@example.com"),
            passwordHash = PasswordHash("hashed"),
            username = Username("testuser")
        )

        every { loadUserPort.loadById(any()) } returns Mono.just(user)
        every { authService.validateCredentials(any(), any()) } returns false

        val request = ChangePasswordRequest(
            currentPassword = "wrongpassword",
            newPassword = "newpassword123",
            confirmPassword = "newpassword123"
        )

        StepVerifier.create(service.changePassword("user-id", request))
            .expectError(AuthenticationException.InvalidCredentialsException::class.java)
            .verify()
    }
})