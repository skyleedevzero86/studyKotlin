package com.sleekydz86.oauth.unit.security

import com.sleekydz86.oauth.global.config.JwtProperties
import com.sleekydz86.oauth.global.security.jwt.JwtTokenProvider
import com.sleekydz86.oauth.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("JwtTokenProvider 단위 테스트 - JWT 발급·검증 역할")
class JwtTokenProviderUnitTest {

    private val jwtTokenProvider = JwtTokenProvider(
        JwtProperties(
            secret = "hello-my-name-is-dev-leeky-im-korean-web-dev-and-youtuber",
            accessTokenExpireTime = 3_600_000L,
            signingAlgorithm = "HS256",
        ),
    )

    @Test
    @DisplayName("역할: 토큰 발급 - username과 role 클레임을 포함한다")
    fun createAccessTokenContainsClaims() {
        val name = "JWT - 클레임 포함 발급"
        TestLog.start(name)

        // given
        TestLog.given("JwtTokenProvider", "username=admin, role=ROLE_ADMIN")

        // when
        TestLog.`when`("createAccessToken", "액세스 토큰 생성")
        val token = jwtTokenProvider.createAccessToken("admin", "ROLE_ADMIN")
        val claims = jwtTokenProvider.getClaims(token)

        // then
        TestLog.then("getClaims", "subject=admin, role=ROLE_ADMIN, tokenType=ACCESS")
        assertNotNull(token)
        assertEquals("admin", claims.subject)
        assertEquals("ROLE_ADMIN", claims["role"])
        assertEquals("ACCESS", claims["tokenType"])

        TestLog.end(name)
    }

    @Test
    @DisplayName("역할: 토큰 검증 - 만료 시간이 설정된다")
    fun createAccessTokenHasExpiration() {
        val name = "JWT - 만료 시간 설정"
        TestLog.start(name)

        // given
        TestLog.given("JwtTokenProvider", "expireTime=3600000ms")

        // when
        TestLog.`when`("createAccessToken", "토큰 생성 후 claims 조회")
        val token = jwtTokenProvider.createAccessToken("user1", "ROLE_USER")
        val claims = jwtTokenProvider.getClaims(token)

        // then
        TestLog.then("getClaims", "expiration > issuedAt")
        assertNotNull(claims.expiration)
        assertNotNull(claims.issuedAt)
        assert(claims.expiration.after(claims.issuedAt))

        TestLog.end(name)
    }
}
