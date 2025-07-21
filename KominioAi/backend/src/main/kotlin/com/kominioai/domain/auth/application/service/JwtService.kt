package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.domain.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {
    @Value("\${app.jwt.secret}")
    private lateinit var secret: String

    @Value("\${app.jwt.expiration}")
    private var expiration: Long = 0

    @Value("\${app.jwt.refresh-expiration:86400}")
    private var refreshExpiration: Long = 86400

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration * 1000)

        return Jwts.builder()
            .subject(user.id.value)
            .claim("email", user.email.value)
            .claim("username", user.username.value)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun generateAccessToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration * 1000)

        return Jwts.builder()
            .subject(user.id.value)
            .claim("email", user.email.value)
            .claim("username", user.username.value)
            .claim("type", "ACCESS")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshExpiration * 1000)

        return Jwts.builder()
            .subject(user.id.value)
            .claim("email", user.email.value)
            .claim("username", user.username.value)
            .claim("type", "REFRESH")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun getAccessTokenExpirySeconds(): Long {
        return expiration
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getClaimsFromToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    fun getUserIdFromToken(token: String): String? {
        return getClaimsFromToken(token)?.subject
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            claims?.expiration?.before(Date()) ?: true
        } catch (e: Exception) {
            true
        }
    }
}