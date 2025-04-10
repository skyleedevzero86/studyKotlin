package com.karchitecture.auth.infrastructure

import com.karchitecture.auth.application.TokenProvider
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secretKey: String,

    @Value("\${jwt.expiration-period}")
    private val expirationPeriod: Long,

    ) : TokenProvider {

    private lateinit var key: Key

    @PostConstruct
    fun init() {
        key = Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    override fun create(id: Long): String {
        return Jwts.builder()
            .id(id.toString())
            .issuedAt(issuedAt())
            .expiration(expiredAt())
            .signWith(key)
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
        return try {
            Jwts.parser()
                .setSigningKey(secretKey.toByteArray())
                .build()
                .parseClaimsJws(token)
                .body["id", Long::class.java]
        } catch (e: SecurityException) {
            throw IllegalArgumentException("토큰의 서명이 잘못 되었습니다.")
        } catch (e: MalformedJwtException) {
            throw IllegalArgumentException("토큰의 형식이 올바르지 않습니다.")
        } catch (e: ExpiredJwtException) {
            throw IllegalArgumentException("토큰이 만료되었습니다.")
        } catch (e: UnsupportedJwtException) {
            throw IllegalArgumentException("지원하지 않는 토큰의 형식입니다.")
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("토큰의 값이 유효하지 않습니다.")
        }
    }
}
