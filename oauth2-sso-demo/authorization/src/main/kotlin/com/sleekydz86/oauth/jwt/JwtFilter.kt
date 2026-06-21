package com.sleekydz86.oauth.jwt

import com.sleekydz86.oauth.util.JWTUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.web.filter.OncePerRequestFilter

// Step 5: SecurityConfig에서 필터 체인에 등록
class JwtFilter(
    private val jwtUtil: JWTUtil,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null) {
            runCatching {
                val claims = jwtUtil.getClaims(token)
                val username = claims.subject
                val role = claims.get("role", String::class.java)
                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                val principal = User(username, "", authorities)
                val authentication = UsernamePasswordAuthenticationToken(principal, token, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        if (!bearerToken.startsWith(BEARER_PREFIX)) {
            return null
        }
        return bearerToken.removePrefix(BEARER_PREFIX).trim()
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}
