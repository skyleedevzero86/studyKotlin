package com.kominioai.domain.survey.domain.event

import java.time.LocalDateTime
import java.util.UUID

abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val occurredOn: LocalDateTime = LocalDateTime.now()
)