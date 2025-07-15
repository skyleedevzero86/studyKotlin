package com.kominioai.global.util

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
class QuizWebSocketHandler : WebSocketHandler {

    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun handle(session: WebSocketSession): Mono<Void> {
        val surveyId = session.handshakeInfo.uri.path.split("/").last()
        sessions[session.id] = session

        return session.receive()
            .doOnNext { message ->
              
            }
            .doFinally {
                sessions.remove(session.id)
            }
            .then()
    }

    fun sendToSurvey(surveyId: String, message: String) {
        sessions.values.forEach { session ->
            if (session.isOpen) {
                session.send(Mono.just(session.textMessage(message))).subscribe()
            }
        }
    }
}