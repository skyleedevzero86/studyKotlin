package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.domain.model.event.SurveyEvent

interface EventPublisher {
    suspend fun publish(event: SurveyEvent)
}