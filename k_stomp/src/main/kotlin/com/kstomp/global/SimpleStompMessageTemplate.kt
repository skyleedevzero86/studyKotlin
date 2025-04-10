package com.kstomp.global

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class SimpleStompMessageTemplate(
    private val template: SimpMessagingTemplate
) : StompMessageTemplate {
    override fun convertAndSend(type: String, destination: String, payload: Any) {
        template.convertAndSend("/$type/$destination", payload)
    }
}