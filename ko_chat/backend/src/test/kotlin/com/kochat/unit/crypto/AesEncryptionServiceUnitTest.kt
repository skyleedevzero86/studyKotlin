package com.kochat.unit.crypto

import com.kochat.global.config.EncryptionProperties
import com.kochat.global.crypto.AesEncryptionService
import com.kochat.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@DisplayName("AesEncryptionService 단위 테스트 - 민감 정보 AES-256-GCM 암호화 역할")
class AesEncryptionServiceUnitTest {

    private val encryptionService = AesEncryptionService(
        EncryptionProperties(secret = "oauth2-sso-demo-aes-256-secret-key-for-sensitive-data"),
    )

    @Test
    @DisplayName("역할: 암호화 - 평문 JSON을 Base64 암호문으로 변환한다")
    fun encryptProducesDifferentCipherText() {
        val name = "AES - 평문과 다른 암호문 생성"
        TestLog.start(name)

        // given
        val plainText = """{"role":"USER","status":"ACTIVE"}"""
        TestLog.given("AesEncryptionService", "plainText=$plainText")

        // when
        TestLog.`when`("encrypt", "AES-256-GCM 암호화")
        val encrypted = encryptionService.encrypt(plainText)

        // then
        TestLog.then("encrypt", "평문과 다른 Base64 문자열 반환")
        assertNotEquals(plainText, encrypted)

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 복호화 - 암호문을 원본 평문으로 복원한다")
    fun encryptAndDecryptRoundTrip() {
        val name = "AES - 암호화/복호화 왕복"
        TestLog.start(name)

        // given
        val plainText = """{"role":"ADMIN","status":"ACTIVE","passwordExpired":false}"""
        TestLog.given("AesEncryptionService", "plainText=$plainText")

        // when
        TestLog.`when`("encrypt → decrypt", "왕복 처리")
        val encrypted = encryptionService.encrypt(plainText)
        val decrypted = encryptionService.decrypt(encrypted)

        // then
        TestLog.then("decrypt", "원본과 동일한 JSON")
        assertEquals(plainText, decrypted)

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 암호화 - 동일 평문도 IV가 달라 매번 다른 암호문이 생성된다")
    fun encryptGeneratesUniqueCipherText() {
        val name = "AES - IV 랜덤으로 매번 다른 암호문"
        TestLog.start(name)

        // given
        val plainText = """{"role":"USER"}"""
        TestLog.given("AesEncryptionService", "동일 plainText 2회 암호화")

        // when
        TestLog.`when`("encrypt", "2회 실행")
        val first = encryptionService.encrypt(plainText)
        val second = encryptionService.encrypt(plainText)

        // then
        TestLog.then("encrypt", "두 암호문 모두 복호화 가능, 서로 다름")
        assertNotEquals(first, second)
        assertEquals(plainText, encryptionService.decrypt(first))
        assertEquals(plainText, encryptionService.decrypt(second))

        TestLog.end(name)
    }
}
