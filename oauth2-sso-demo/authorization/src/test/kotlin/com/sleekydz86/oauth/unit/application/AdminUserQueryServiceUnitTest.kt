package com.sleekydz86.oauth.unit.application

import com.sleekydz86.oauth.domain.user.model.User
import com.sleekydz86.oauth.domain.user.model.UserRole
import com.sleekydz86.oauth.domain.user.model.UserStatus
import com.sleekydz86.oauth.global.application.user.AdminUserQueryService
import com.sleekydz86.oauth.global.config.EncryptionProperties
import com.sleekydz86.oauth.global.crypto.AesEncryptionService
import com.sleekydz86.oauth.support.InMemoryUserPersistencePort
import com.sleekydz86.oauth.support.TestLog
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("AdminUserQueryService 단위 테스트 - 관리자 사용자 목록·민감정보 암호화 역할")
class AdminUserQueryServiceUnitTest {

    private lateinit var persistencePort: InMemoryUserPersistencePort
    private lateinit var queryService: AdminUserQueryService
    private lateinit var aesEncryptionService: AesEncryptionService

    @BeforeEach
    fun setUp() {
        persistencePort = InMemoryUserPersistencePort()
        aesEncryptionService = AesEncryptionService(
            EncryptionProperties(secret = "oauth2-sso-demo-aes-256-secret-key-for-sensitive-data"),
        )
        queryService = AdminUserQueryService(
            userPersistencePort = persistencePort,
            aesEncryptionService = aesEncryptionService,
            objectMapper = JsonMapper.builder().build(),
        )
    }

    @Test
    @DisplayName("역할: 사용자 목록 - username은 평문, 민감정보는 encryptedPayload로 내려준다")
    fun findAllUserSummariesEncryptsSensitiveFields() {
        val name = "AdminUserQuery - username 평문 + encryptedPayload"
        TestLog.start(name)

        // given
        TestLog.given("AdminUserQueryService", "ACTIVE user1 1명 저장")
        persistencePort.save(
            User.restore(
                id = 1L,
                username = "user1",
                password = "encoded:pass",
                displayName = null,
                role = UserRole.USER,
                status = UserStatus.ACTIVE,
                createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                passwordChangedAt = Instant.parse("2026-01-01T00:00:00Z"),
                passwordChangeFailCount = 0,
                loginFailCount = 0,
                lastLoginAt = null,
            ),
        )

        // when
        TestLog.`when`("findAllUserSummaries", "목록 조회")
        val summaries = queryService.findAllUserSummaries()

        // then
        TestLog.then("findAllUserSummaries", "username=user1, payload 복호화 시 role/status 포함")
        assertEquals(1, summaries.size)
        val summary = summaries.first()
        assertEquals("user1", summary.username)
        assertTrue(summary.encryptedPayload.isNotBlank())

        val decryptedJson = aesEncryptionService.decrypt(summary.encryptedPayload)
        assertTrue(decryptedJson.contains("\"role\":\"USER\""))
        assertTrue(decryptedJson.contains("\"status\":\"ACTIVE\""))
        assertNotEquals(decryptedJson, summary.username)

        TestLog.end(name)
    }
}
