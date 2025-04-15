package com.kstomp.global

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class RabbitMqStompMessageTemplate(
    private val rabbitTemplate: RabbitTemplate
) : StompMessageTemplate {
    override fun convertAndSend(type: String, destination: String, payload: Any) {
        when (type) {
            "topic" -> rabbitTemplate.convertAndSend("amq.topic", destination, payload)
            "queue" -> rabbitTemplate.convertAndSend("amq.direct", destination, payload)
            "exchange" -> rabbitTemplate.convertAndSend(destination, "", payload)
            else -> rabbitTemplate.convertAndSend(type, destination, payload)
        }
    }
}