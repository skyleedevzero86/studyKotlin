package com.sleekydz86.oauth.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JWTUtil(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-expire-time}") private val accessTokenExpireTime: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun createAccessToken(username: String, role: String): String {
        val now = Date()
        val expireDate = Date(now.time + accessTokenExpireTime)

        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .claim("tokenType", "ACCESS")
            .issuedAt(now)
            .expiration(expireDate)
            .signWith(key)
            .compact()
    }

    fun getClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
