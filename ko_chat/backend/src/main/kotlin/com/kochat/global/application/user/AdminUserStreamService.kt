package com.kochat.global.application.user

import com.kochat.global.event.UserChangedEvent
import com.kochat.global.event.UserListRefreshEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.CopyOnWriteArrayList

@Service
class AdminUserStreamService(
    private val adminUserQueryService: AdminUserQueryService,
) {
    private val emitters = CopyOnWriteArrayList<SseEmitter>()
    private val objectMapper = ObjectMapper()

    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters.add(emitter)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        sendSnapshot(emitter)
        return emitter
    }

    @EventListener
    fun onUserChanged(event: UserChangedEvent) {
        broadcast()
    }

    @EventListener
    fun onUserListRefresh(event: UserListRefreshEvent) {
        broadcast()
    }

    private fun broadcast() {
        val payload = objectMapper.writeValueAsString(adminUserQueryService.findAllUserSummaries())
        emitters.forEach { emitter ->
            runCatching {
                emitter.send(SseEmitter.event().name("users").data(payload))
            }.onFailure {
                emitters.remove(emitter)
            }
        }
    }

    private fun sendSnapshot(emitter: SseEmitter) {
        val payload = objectMapper.writeValueAsString(adminUserQueryService.findAllUserSummaries())
        emitter.send(SseEmitter.event().name("users").data(payload))
    }
}
