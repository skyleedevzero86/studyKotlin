package com.kochat.feature

import com.kochat.support.FeatureTestSupport
import com.kochat.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.assertTrue

@DisplayName("로그인 기능 테스트 - Login API E2E 역할")
class UserLoginFeatureTest : FeatureTestSupport() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("역할: POST /api/v1/login - ACTIVE 회원은 JWT accessToken을 받는다")
    fun loginActiveUserReturnsToken() {
        val name = "POST /api/v1/login - JWT 발급"
        TestLog.start(name)

        // given
        TestLog.given("Login API", "ACTIVE login-user / pass1234!")
        registerActiveUser("login-user", "pass1234!")
        val body = """{"username":"login-user","password":"pass1234!"}"""

        // when
        TestLog.`when`("POST /api/v1/login", "로그인 요청")
        val result = mockMvc.post("/api/v1/login") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn()

        // then
        TestLog.then("POST /api/v1/login", "200 OK, accessToken 포함")
        assertTrue(result.response.status == 200)
        assertTrue(result.response.contentAsString.contains("accessToken"))

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: POST /api/v1/login - PENDING 회원은 승인 대기로 거부된다")
    fun loginPendingUserDenied() {
        val name = "POST /api/v1/login - PENDING 거부"
        TestLog.start(name)

        // given
        TestLog.given("Login API", "PENDING pending-user / pass1234!")
        registerPendingUser("pending-user", "pass1234!")
        val body = """{"username":"pending-user","password":"pass1234!"}"""

        // when
        TestLog.`when`("POST /api/v1/login", "미승인 회원 로그인")
        val result = mockMvc.post("/api/v1/login") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn()

        // then
        TestLog.then("POST /api/v1/login", "403 FORBIDDEN, LOGIN_DENIED")
        assertTrue(result.response.status == 403)
        assertTrue(result.response.contentAsString.contains("LOGIN_DENIED"))

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: POST /api/v1/login - 잘못된 비밀번호는 인증 실패한다")
    fun loginWrongPasswordFails() {
        val name = "POST /api/v1/login - 비밀번호 오류"
        TestLog.start(name)

        // given
        TestLog.given("Login API", "ACTIVE login-user, 잘못된 비밀번호")
        registerActiveUser("login-user", "pass1234!")
        val body = """{"username":"login-user","password":"wrong-pass"}"""

        // when / then
        TestLog.`when`("POST /api/v1/login", "오류 비밀번호로 로그인")
        val result = mockMvc.post("/api/v1/login") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn()

        TestLog.then("POST /api/v1/login", "401 Unauthorized, AUTHENTICATION_FAILED")
        assertTrue(result.response.status == 401)
        assertTrue(result.response.contentAsString.contains("AUTHENTICATION_FAILED"))

        TestLog.end(name)
    }
}
