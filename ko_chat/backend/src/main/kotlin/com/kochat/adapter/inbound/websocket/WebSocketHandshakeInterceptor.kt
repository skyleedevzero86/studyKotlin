package com.kochat.adapter.inbound.websocket

import com.kochat.domain.user.model.UserStatus
import com.kochat.domain.user.port.out.UserPersistencePort
import com.kochat.global.security.jwt.JwtTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class WebSocketHandshakeInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userPersistencePort: UserPersistencePort,
) : HandshakeInterceptor {

    private val logger = LoggerFactory.getLogger(WebSocketHandshakeInterceptor::class.java)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        return try {
            val query = request.uri.query ?: return false
            val params = parseQuery(query)
            val token = params["token"] ?: return false

            val claims = jwtTokenProvider.getClaims(token)
            val username = claims.subject
            val tokenType = claims.get("tokenType", String::class.java)

            if (tokenType != "ACCESS") {
                return false
            }

            val user = userPersistencePort.findByUsername(username)
            if (user == null || user.status != UserStatus.ACTIVE || user.id == null) {
                return false
            }

            attributes["userId"] = user.id
            true
        } catch (e: Exception) {
            logger.error("WebSocket 핸드셰이크 중 오류가 발생했습니다", e)
            false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
        if (exception != null) {
            logger.error("WebSocket 핸드셰이크 예외", exception)
        }
    }

    private fun parseQuery(query: String): Map<String, String> =
        query.split("&")
            .mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
}
