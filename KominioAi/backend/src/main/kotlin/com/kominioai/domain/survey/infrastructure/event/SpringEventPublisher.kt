package com.kominioai.domain.survey.infrastructure.event

import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.domain.model.event.SurveyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : EventPublisher {

    override suspend fun publish(event: SurveyEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}