package com.sleekydz86.multitenant.web

import com.sleekydz86.multitenant.domain.MissingTenant
import com.sleekydz86.multitenant.domain.TenantContext
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper

@Component
class TenantFilter(private val mapper: ObjectMapper) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val tenant = request.getHeader("X-Tenant-Id")
        if (tenant.isNullOrBlank()) {
            log.warn("테넌트 헤더가 없습니다. header=X-Tenant-Id path={}", request.requestURI)
            response.status = 400
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            mapper.writeValue(response.writer, ApiError(MissingTenant().message ?: "요청에 테넌트가 없습니다."))
            return
        }
        TenantContext.call(tenant) {
            chain.doFilter(request, response)
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(TenantFilter::class.java)
    }
}
