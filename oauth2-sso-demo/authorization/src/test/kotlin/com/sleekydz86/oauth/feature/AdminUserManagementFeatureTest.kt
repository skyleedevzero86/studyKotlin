package com.sleekydz86.oauth.feature

import com.sleekydz86.oauth.global.crypto.AesEncryptionService
import com.sleekydz86.oauth.support.FeatureTestSupport
import com.sleekydz86.oauth.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.databind.json.JsonMapper
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("관리자 사용자 관리 기능 테스트 - Admin API E2E 역할")
class AdminUserManagementFeatureTest : FeatureTestSupport() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var aesEncryptionService: AesEncryptionService

    @Test
    @DisplayName("역할: GET /api/v1/admin/users - ADMIN은 암호화된 사용자 목록을 조회한다")
    fun adminCanListEncryptedUsers() {
        val name = "GET /admin/users - 암호화 목록"
        TestLog.start(name)

        // given
        TestLog.given("Admin API", "admin 토큰, user1 ACTIVE 등록")
        val admin = registerAdmin("list-admin", "admin1234!@#")
        registerActiveUser("user1", "pass1234!")
        val token = bearerToken(admin.username, "ROLE_ADMIN")

        // when
        TestLog.`when`("GET /api/v1/admin/users", "관리자 목록 조회")
        val result = mockMvc.get("/api/v1/admin/users") {
            header("Authorization", token)
        }.andReturn()

        // then
        TestLog.then("GET /api/v1/admin/users", "200 OK, username 평문 + encryptedPayload")
        assertEquals(200, result.response.status)
        assertTrue(result.response.contentAsString.contains("user1"))
        assertTrue(result.response.contentAsString.contains("encryptedPayload"))

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: GET /api/v1/admin/users - USER 권한은 403 Forbidden")
    fun userCannotAccessAdminList() {
        val name = "GET /admin/users - USER 403"
        TestLog.start(name)

        // given
        TestLog.given("Admin API", "USER 토큰")
        registerActiveUser("normal-user", "pass1234!")
        val token = bearerToken("normal-user", "ROLE_USER")

        // when
        TestLog.`when`("GET /api/v1/admin/users", "일반 사용자 접근")
        val result = mockMvc.get("/api/v1/admin/users") {
            header("Authorization", token)
        }.andReturn()

        // then
        TestLog.then("GET /api/v1/admin/users", "403 Forbidden")
        assertEquals(403, result.response.status)

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: POST /api/v1/admin/users/{username}/approve - PENDING 회원을 승인한다")
    fun adminApprovesPendingUser() {
        val name = "POST /admin/users/approve - 승인"
        TestLog.start(name)

        // given
        TestLog.given("Admin API", "admin 토큰, approve-me PENDING")
        val admin = registerAdmin("approve-admin", "admin1234!@#")
        registerPendingUser("approve-me", "pass1234!")
        val token = bearerToken(admin.username, "ROLE_ADMIN")

        // when
        TestLog.`when`("POST /api/v1/admin/users/approve-me/approve", "승인 요청")
        val result = mockMvc.post("/api/v1/admin/users/approve-me/approve") {
            header("Authorization", token)
            contentType = MediaType.APPLICATION_JSON
            content = """{"role":"USER"}"""
        }.andReturn()

        // then
        TestLog.then("approve", "200 OK, status=ACTIVE")
        assertEquals(200, result.response.status)
        assertTrue(result.response.contentAsString.contains("ACTIVE"))

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: GET /api/v1/admin/users - encryptedPayload는 AES 복호화 가능한 JSON이다")
    fun encryptedPayloadIsDecryptableJson() {
        val name = "GET /admin/users - payload 복호화 검증"
        TestLog.start(name)

        // given
        TestLog.given("Admin API", "admin + crypto-user 등록")
        val admin = registerAdmin("crypto-admin", "admin1234!@#")
        registerActiveUser("crypto-user", "pass1234!")
        val token = bearerToken(admin.username, "ROLE_ADMIN")

        // when
        TestLog.`when`("GET /api/v1/admin/users", "목록 조회 후 payload 추출")
        val result = mockMvc.get("/api/v1/admin/users") {
            header("Authorization", token)
        }.andReturn()

        val responseBody = result.response.contentAsString
        val summaries = JsonMapper.builder().build().readTree(responseBody)
        val encryptedPayload = summaries.first { node ->
            node.get("username").asText() == "crypto-user"
        }.get("encryptedPayload").asText()

        val decrypted = aesEncryptionService.decrypt(encryptedPayload)

        // then
        TestLog.then("AES decrypt", "role/status JSON 포함")
        assertTrue(decrypted.contains("\"role\":\"USER\""))
        assertTrue(decrypted.contains("\"status\":\"ACTIVE\""))

        TestLog.end(name)
    }
}
