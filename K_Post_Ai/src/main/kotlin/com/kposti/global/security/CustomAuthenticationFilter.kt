package com.kposti.global.security

import com.kposti.domain.member.entity.Member
import com.kposti.domain.member.service.MemberService
import com.kposti.global.https.ReqData
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: ReqData
) : OncePerRequestFilter() {

    data class AuthTokens(val apiKey: String, val accessToken: String)

    private fun getAuthTokensFromRequest(): AuthTokens? {
        rq.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substringAfter("Bearer ")
            ?.split(" ", limit = 2)
            ?.takeIf { it.size == 2 }
            ?.let { return AuthTokens(it[0], it[1]) }

        val apiKey = rq.getCookieValue("apiKey")
        val accessToken = rq.getCookieValue("accessToken")

        return if (apiKey != null && accessToken != null) AuthTokens(apiKey, accessToken) else null
    }

    private fun refreshAccessToken(member: Member) = rq.refreshAccessToken(member)

    private fun refreshAccessTokenByApiKey(apiKey: String): Member? =
        memberService.findByApiKey(apiKey)
            .orElse(null)
            ?.also { refreshAccessToken(it) }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI

        if (!uri.startsWith("/api/") || uri in listOf("/api/v1/members/login", "/api/v1/members/logout", "/api/v1/members/join")) {
            filterChain.doFilter(request, response)
            return
        }

        val authTokens = getAuthTokensFromRequest()
        val member = authTokens?.let {
            memberService.getMemberFromAccessToken(it.accessToken)
                ?: refreshAccessTokenByApiKey(it.apiKey)
        }

        member?.let { rq.setLogin(it) }

        filterChain.doFilter(request, response)
    }
}