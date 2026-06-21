package com.sleekydz86.oauth.global.security.jwt

import com.sleekydz86.oauth.domain.user.model.UserStatus
import com.sleekydz86.oauth.domain.user.port.out.UserPersistencePort
import com.sleekydz86.oauth.global.exception.ApiErrorResponse
import com.sleekydz86.oauth.global.exception.ErrorCode
import com.sleekydz86.oauth.global.exception.ErrorResponseWriter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userPersistencePort: UserPersistencePort,
    private val errorResponseWriter: ErrorResponseWriter,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorization = request.getHeader(AUTHORIZATION_HEADER)

        if (authorization == null) {
            filterChain.doFilter(request, response)
            return
        }

        if (!authorization.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, ErrorCode.INVALID_TOKEN)
            return
        }

        val accessToken = authorization.substring(BEARER_PREFIX.length)

        try {
            val claims = jwtTokenProvider.getClaims(accessToken)

            val username = claims.subject
            val role = claims.get("role", String::class.java)
            val tokenType = claims.get("tokenType", String::class.java)

            if (tokenType != ACCESS_TOKEN_TYPE) {
                writeUnauthorized(response, ErrorCode.INVALID_TOKEN)
                return
            }

            val user = userPersistencePort.findByUsername(username)
            if (user == null || user.status != UserStatus.ACTIVE) {
                writeUnauthorized(response, ErrorCode.INACTIVE_ACCOUNT)
                return
            }

            if (user.isPasswordExpired()) {
                writeUnauthorized(response, ErrorCode.PASSWORD_EXPIRED)
                return
            }

            val authorities = listOf(SimpleGrantedAuthority(role))
            val auth: Authentication = UsernamePasswordAuthenticationToken(username, null, authorities)
            SecurityContextHolder.getContext().authentication = auth

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            writeUnauthorized(response, ErrorCode.EXPIRED_TOKEN)
        }
    }

    private fun writeUnauthorized(response: HttpServletResponse, errorCode: ErrorCode) {
        errorResponseWriter.write(
            response,
            HttpStatus.UNAUTHORIZED.value(),
            ApiErrorResponse(error = errorCode.defaultMessage, code = errorCode.name),
        )
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val ACCESS_TOKEN_TYPE = "ACCESS"
    }
}
