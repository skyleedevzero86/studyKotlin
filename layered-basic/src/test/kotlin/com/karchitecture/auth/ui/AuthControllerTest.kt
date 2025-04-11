package com.karchitecture.auth.ui

import com.karchitecture.auth.application.request.SignInRequest
import com.karchitecture.auth.application.request.SignUpRequest
import com.karchitecture.auth.domain.Auth
import com.karchitecture.auth.domain.service.AuthRepository
import com.karchitecture.auth.domain.service.Encryptor
import com.karchitecture.helper.IntegrationHelper
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AuthControllerTest.MockConfig::class)
class AuthControllerTest : IntegrationHelper() {

    companion object {
        // lateinit은 static 영역에서 쓰기 위해 companion object에 선언
        lateinit var authRepository: AuthRepository
        lateinit var encryptor: Encryptor
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun mockAuthRepository(): AuthRepository {
            authRepository = Mockito.mock(AuthRepository::class.java)
            return authRepository
        }

        @Bean
        fun mockEncryptor(): Encryptor {
            encryptor = Mockito.mock(Encryptor::class.java)
            return encryptor
        }
    }

    @Test
    fun `회원가입을 진행한다`() {
        // given
        val request = SignUpRequest("username", "password")

        // when
        val response = RestAssured.given().log().all()
            .`when`()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/auth/sign-up")
            .then().log().all()
            .extract()

        // then
        assertEquals(HttpStatus.CREATED.value(), response.statusCode())
    }

    @Test
    fun `로그인을 진행한다`() {
        // given
        val encryptedPassword = "encryptedPassword"
        given(encryptor.encrypt("password")).willReturn(encryptedPassword)
        val auth = Auth.signUpWithEncryption("username", "password", encryptor)
        given(authRepository.findByUsername("username")).willReturn(auth)

        val request = SignInRequest("username", "password")

        // when
        val response = RestAssured.given().log().all()
            .`when`()
            .contentType(ContentType.JSON)
            .body(request)
            .get("/auth/sign-in")
            .then().log().all()
            .extract()

        // then
        assertEquals(HttpStatus.OK.value(), response.statusCode())
    }
}