package com.sleekydz86.oauth.global.security.jwt

import com.sleekydz86.oauth.global.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    jwtProperties: JwtProperties,
) {
    private val accessTokenExpireTime: Long = jwtProperties.accessTokenExpireTime
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

    init {
        require(jwtProperties.signingAlgorithm == "HS256") {
            "현재 authorization 모듈은 단일 애플리케이션용 HS256(대칭키)만 지원합니다."
        }
    }

    fun createAccessToken(username: String, role: String): String {
        val now = Date()
        val expireDate = Date(now.time + accessTokenExpireTime)

        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .claim("tokenType", "ACCESS")
            .issuedAt(now)
            .expiration(expireDate)
            .signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    fun getClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
