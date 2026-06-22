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

@DisplayName("회원 가입 기능 테스트 - Join API E2E 역할")
class UserJoinFeatureTest : FeatureTestSupport() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("역할: POST /api/v1/join - 신규 회원을 PENDING 상태로 등록한다")
    fun joinCreatesPendingUser() {
        val name = "POST /api/v1/join - PENDING 등록"
        TestLog.start(name)

        // given
        TestLog.given("Join API", "username=join-user, password=pass1234!, 미등록 상태")
        val body = """{"username":"join-user","password":"pass1234!"}"""

        // when
        TestLog.`when`("POST /api/v1/join", "회원가입 요청")
        val result = mockMvc.post("/api/v1/join") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn()

        // then
        TestLog.then("POST /api/v1/join", "201 CREATED, status=PENDING")
        assertTrue(result.response.status == 201)
        assertTrue(result.response.contentAsString.contains("PENDING"))

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: POST /api/v1/join - 중복 아이디는 409 CONFLICT")
    fun joinDuplicateUsernameReturnsConflict() {
        val name = "POST /api/v1/join - 중복 409"
        TestLog.start(name)

        // given
        TestLog.given("Join API", "username=dup-user 이미 가입됨")
        registerPendingUser("dup-user", "pass1234!")
        val body = """{"username":"dup-user","password":"other1234!"}"""

        // when
        TestLog.`when`("POST /api/v1/join", "동일 username 재가입")
        val result = mockMvc.post("/api/v1/join") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn()

        // then
        TestLog.then("POST /api/v1/join", "409 CONFLICT")
        assertTrue(result.response.status == 409)

        TestLog.end(name)
    }
}
