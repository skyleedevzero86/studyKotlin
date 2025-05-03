package com.komroonga.global.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = getTokenFromRequest(request)
            logger.debug("추출된 토큰: $token")

            if (token != null && jwtTokenProvider.validateToken(token)) {
                val username = jwtTokenProvider.getUsernameFromToken(token)

                // UserDetailsService를 통해 사용자 정보 로드
                val userDetails = userDetailsService.loadUserByUsername(username)

                // 인증 객체 생성 (실제 권한 정보 포함)
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )

                // SecurityContext에 인증 객체 설정
                SecurityContextHolder.getContext().authentication = authentication
                logger.debug("인증 완료: 사용자 $username, 권한: ${userDetails.authorities}")
            } else if (token != null) {
                logger.debug("유효하지 않은 토큰: $token")
            }
        } catch (e: Exception) {
            logger.error("JWT 인증 처리 중 오류 발생: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        // 1. Authorization 헤더에서 토큰 추출 시도
        val bearerToken = request.getHeader("Authorization")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }

        // 2. 쿠키에서 토큰 추출 시도
        val cookie = request.cookies?.find { it.name == "jwt" }
        if (cookie != null) {
            return cookie.value
        }

        // 3. 요청 파라미터에서 토큰 추출 시도
        val tokenParam = request.getParameter("token")
        if (!tokenParam.isNullOrEmpty()) {
            return tokenParam
        }

        return null
    }
}