package com.sleekydz86.oauth.feature

import com.sleekydz86.oauth.support.FeatureTestSupport
import com.sleekydz86.oauth.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("비밀번호 변경 기능 테스트 - User Password API E2E 역할")
class UserPasswordChangeFeatureTest : FeatureTestSupport() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("역할: POST /api/v1/user/password/change - 현재 비밀번호 확인 후 변경한다")
    fun changePasswordSuccess() {
        val name = "POST /user/password/change - 성공"
        TestLog.start(name)

        // given
        TestLog.given("Password API", "ACTIVE pwd-user / pass1234!")
        registerActiveUser("pwd-user", "pass1234!")
        val body = """
            {
              "username":"pwd-user",
              "currentPassword":"pass1234!",
              "newPassword":"newpass5678!"
            }
        """.trimIndent()

        // when
        TestLog.`when`("POST /api/v1/user/password/change", "비밀번호 변경 요청")
        val result = mockMvc.post("/api/v1/user/password/change") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn()

        // then
        TestLog.then("change password", "200 OK")
        assertEquals(200, result.response.status)
        assertTrue(result.response.contentAsString.contains("변경"))

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: POST /api/v1/user/password/change - 현재 비밀번호 오류 시 400")
    fun changePasswordWrongCurrentPassword() {
        val name = "POST /user/password/change - 현재 비밀번호 오류"
        TestLog.start(name)

        // given
        TestLog.given("Password API", "ACTIVE pwd-user, wrong current password")
        registerActiveUser("pwd-user", "pass1234!")
        val body = """
            {
              "username":"pwd-user",
              "currentPassword":"wrong-pass",
              "newPassword":"newpass5678!"
            }
        """.trimIndent()

        // when
        TestLog.`when`("POST /api/v1/user/password/change", "잘못된 현재 비밀번호")
        val result = mockMvc.post("/api/v1/user/password/change") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andReturn()

        // then
        TestLog.then("change password", "400 BAD_REQUEST")
        assertEquals(400, result.response.status)

        TestLog.end(name)
    }
}
