package com.komroonga.global.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val _expiration: Long
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secretKey: SecretKey

    val expiration: Long
        get() = _expiration

    init {
        val secretBytes = secret.toByteArray(Charsets.UTF_8)
        if (secretBytes.size < 32) {
            logger.error("JWT secret key is too short: {} bytes. Must be at least 32 bytes (256 bits).", secretBytes.size)
            throw IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits)")
        }
        secretKey = Keys.hmacShaKeyFor(secretBytes)
        logger.info("JWT Secret loaded: {} bytes", secretBytes.size)
        logger.info("JWT Expiration: {} ms", _expiration)
    }

    fun generateToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + _expiration)
        logger.debug("Generating token for user: {}", username)
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
            logger.debug("Token validated successfully")
            true
        } catch (e: Exception) {
            logger.error("JWT 토큰 검증 실패: ${e.message}", e)
            false
        }
    }
}