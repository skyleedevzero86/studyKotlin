package com.kominioai.adapter.`in`.web


import com.kominioai.domain.auth.application.dto.LoginRequest
import com.kominioai.domain.auth.application.dto.RegisterRequest
import com.kominioai.domain.auth.application.port.`in`.AuthUseCase
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(AuthController::class)
class AuthControllerIntegrationTest : StringSpec({
    lateinit var webTestClient: WebTestClient
    lateinit var authUseCase: AuthUseCase

    beforeTest {
        authUseCase = mockk()
        webTestClient = WebTestClient.bindToController(AuthController(authUseCase)).build()
    }

    "POST /api/auth/login should return 200 for valid request" {
        val loginRequest = LoginRequest("test@example.com", "password", false, null, null)
        val loginResponse = mockk<com.kominioai.domain.auth.application.dto.LoginResponse>()

        every { authUseCase.login(any()) } returns Mono.just(loginResponse)

        webTestClient.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
    }

    "POST /api/auth/register should return 200 for valid request" {
        val registerRequest = RegisterRequest(
            email = "new@example.com",
            username = "newuser",
            password = "password123",
            confirmPassword = "password123"
        )
        val registerResponse = mockk<com.kominioai.domain.auth.application.dto.RegisterResponse>()

        every { authUseCase.register(any()) } returns Mono.just(registerResponse)

        webTestClient.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isOk
    }
})