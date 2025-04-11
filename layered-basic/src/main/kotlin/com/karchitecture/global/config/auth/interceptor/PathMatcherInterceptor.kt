package com.karchitecture.global.config.auth.interceptor

import com.karchitecture.global.config.auth.support.HttpMethod
import com.karchitecture.global.config.auth.support.PathContainer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor

class PathMatcherInterceptor(
    private val handlerInterceptor: HandlerInterceptor
) : HandlerInterceptor {

    private val pathContainer = PathContainer()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        if (pathContainer.isNotIncludedPath(request.servletPath, request.method)) {
            return true
        }
        return handlerInterceptor.preHandle(request, response, handler)
    }

    fun addPathPatterns(pathPattern: String, vararg httpMethod: HttpMethod): PathMatcherInterceptor {
        pathContainer.addIncludePatterns(pathPattern, *httpMethod)
        return this
    }

    fun excludePathPattern(pathPattern: String, vararg httpMethod: HttpMethod): PathMatcherInterceptor {
        pathContainer.addExcludePatterns(pathPattern, *httpMethod)
        return this
    }
}