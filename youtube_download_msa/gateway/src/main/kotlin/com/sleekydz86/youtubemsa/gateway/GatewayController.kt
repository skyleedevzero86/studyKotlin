package com.sleekydz86.youtubemsa.gateway

import com.sleekydz86.youtubemsa.shared.StudyException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GatewayController(private val proxy: ServiceProxy) {
    @RequestMapping("/api/downloads", "/api/downloads/**")
    fun route(
        request: HttpServletRequest,
        @org.springframework.web.bind.annotation.RequestBody(required = false) body: ByteArray?,
    ): ResponseEntity<ByteArray> {
        val target = upstream(request.method, request.requestURI)
            ?: throw StudyException("게이트웨이에 없는 경로입니다. 경로: ${request.requestURI}", status = 404)
        return proxy.forward(target, request, body)
    }
}
