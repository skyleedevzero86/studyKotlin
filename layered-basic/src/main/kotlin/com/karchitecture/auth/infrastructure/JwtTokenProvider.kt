package com.karchitecture.auth.infrastructure

import com.karchitecture.auth.application.TokenProvider
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.expiration-period}")
    private val expirationPeriod: Long,

    ) : TokenProvider {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    override fun create(id: Long): String {
        val claims = Jwts.claims()
            .id(id.toString())
            .build()

        return Jwts.builder()
            .claims(claims)
            .issuedAt(issuedAt())
            .expiration(expiredAt())
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun issuedAt(): Date {
        val now = LocalDateTime.now()
        return Date.from(now.atZone(ZoneId.systemDefault()).toInstant())
    }

    private fun expiredAt(): Date {
        val now = LocalDateTime.now()
        return Date.from(now.plusHours(expirationPeriod).atZone(ZoneId.systemDefault()).toInstant())
    }

    override fun extract(token: String): Long {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .body.id.toLong()
        } catch (exception: JwtException) {
            throw handleTokenException(exception)
        } catch (exception: IllegalArgumentException) {
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("토큰의 값이 유효하지 않습니다.")
        }
    }

    private fun handleTokenException(exception: JwtException): IllegalArgumentException {
        return when (exception) {
            is SecurityException -> throw IllegalArgumentException("토큰의 서명이 잘못 되었습니다.")
            is MalformedJwtException -> throw IllegalArgumentException("토큰의 형식이 올바르지 않습니다.")
            is ExpiredJwtException -> throw IllegalArgumentException("토큰이 만료되었습니다.")
            is UnsupportedJwtException -> throw IllegalArgumentException("지원하지 않는 토큰의 형식입니다.")
            else -> IllegalArgumentException("토큰의 값이 유효하지 않습니다.")
        }
    }
}
