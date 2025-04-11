package com.karchitecture.auth.application

import com.karchitecture.auth.application.request.SignInRequest
import com.karchitecture.auth.application.request.SignUpRequest
import com.karchitecture.auth.domain.Auth
import com.karchitecture.auth.domain.service.AuthRepository
import com.karchitecture.auth.domain.service.Encryptor
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatThrownBy

class AuthServiceTest : BehaviorSpec({
    val authRepository: AuthRepository = mockk()
    val encryptor: Encryptor = mockk()
    val tokenProvider: TokenProvider = mockk()

    val authService = AuthService(
        authRepository,
        encryptor,
        tokenProvider
    )

    Given("회원가입을 할 때") {
        val request = SignUpRequest("username", "password")
        val savedAuth = Auth(id = 1L, username = "username", password = "password")

        When("이미 존재하는 username이면") {
            every { authRepository.existsByUsername(any()) } returns true

            Then("예외를 발생시킨다") {
                assertThatThrownBy {
                    authService.signUp(request)
                }.isInstanceOf(RuntimeException::class.java)
            }
        }

        When("존재하지 않는 username이면") {
            every { authRepository.existsByUsername(any()) } returns false
            every { encryptor.encrypt(any()) } returns "encryptedPassword"
            every { authRepository.save(any()) } returns savedAuth
            every { tokenProvider.create(any()) } returns "token"

            val response = authService.signUp(request)

            Then("가입이 된다") {
                shouldNotThrow<RuntimeException> {
                    authService.signUp(request)
                }
                response.accessToken shouldBe "token"
            }
        }
    }

    Given("로그인을 할 때") {
        val request = SignInRequest("username", "password")
        val savedAuth = Auth(id = 1L, username = "username", password = "password")

        When("아이디가 없다면") {
            every { authRepository.findByUsername(any()) } returns null

            Then("예외를 발생시킨다") {
                assertThatThrownBy {
                    authService.signIn(request)
                }.isInstanceOf(RuntimeException::class.java)
            }
        }

        When("아이디가 존재하는데") {
            every { authRepository.findByUsername(any()) } returns savedAuth

            Then("비밀번호가 틀리면 예외를 발생시킨다") {
                every { encryptor.matches(any(), any()) } returns false

                assertThatThrownBy {
                    authService.signIn(request)
                }.isInstanceOf(RuntimeException::class.java)
            }

            Then("비밀번호가 맞다면 토큰을 반환한다") {
                every { encryptor.matches(any(), any()) } returns true
                every { tokenProvider.create(any()) } returns "token"

                val response = authService.signIn(request)

                response.accessToken shouldBe "token"
            }
        }
    }

})