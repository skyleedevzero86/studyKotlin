package com.sleekydz86.komfa.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.http.HttpMethod
import org.springframework.web.filter.OncePerRequestFilter


class GetMeNoErrorFilter : OncePerRequestFilter(), Ordered {

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!isGetMe(request)) {
            filterChain.doFilter(request, response)
            return
        }
        try {
            filterChain.doFilter(request, response)
        } catch (_: Throwable) {
            response.status = HttpServletResponse.SC_NO_CONTENT
            response.setContentLength(0)
        }
    }

    private fun isGetMe(request: HttpServletRequest): Boolean =
        HttpMethod.GET.matches(request.method) && "/api/me" == request.requestURI
}
