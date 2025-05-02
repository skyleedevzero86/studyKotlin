package com.komroonga.global.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

/**
 * JWT 토큰 생성 및 검증을 담당하는 컴포넌트
 * 함수형 프로그래밍 스타일로 구현
 */
@Component
class JwtTokenProvider {

    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration}")
    val validityInMilliseconds: Long = 0

    /**
     * 사용자명과 권한 목록을 기반으로 JWT 토큰을 생성합니다.
     *
     * @param username 사용자명
     * @param roles 사용자 권한 목록 (선택적)
     * @return 생성된 JWT 토큰
     */
    fun createToken(username: String, roles: List<String> = emptyList()): String {
        val claims = Jwts.claims().setSubject(username)

        // 권한 정보가 있으면 추가
        if (roles.isNotEmpty()) {
            claims["roles"] = roles
        }

        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()), SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 토큰에서 사용자명을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자명
     */
    fun getUsernameFromToken(token: String): String =
        getClaimsFromToken(token).subject

    /**
     * 토큰에서 권한 목록을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 권한 목록
     */
    fun getRolesFromToken(token: String): List<String> =
        getClaimsFromToken(token)["roles"]?.let {
            when (it) {
                is List<*> -> it.filterIsInstance<String>()
                else -> emptyList()
            }
        } ?: emptyList()

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    fun validateToken(token: String): Boolean =
        runCatching {
            val claims = getClaimsFromToken(token)
            !claims.expiration.before(Date())
        }.getOrDefault(false)

    /**
     * 토큰에서 클레임을 추출합니다. (내부 사용)
     *
     * @param token JWT 토큰
     * @return JWT 클레임
     */
    private fun getClaimsFromToken(token: String) =
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body

    companion object {
        /**
         * 토큰 만료 검사를 수행합니다.
         *
         * @param token JWT 토큰
         * @param provider JwtTokenProvider 인스턴스
         * @return 만료되었는지 여부
         */
        fun isTokenExpired(token: String, provider: JwtTokenProvider): Boolean =
            runCatching {
                val expiration = provider.getClaimsFromToken(token).expiration
                expiration.before(Date())
            }.getOrDefault(true)
    }
}