package com.sleekydz86.rag.domain.event

import java.time.Instant

sealed class DomainEvent {
    data class DocumentUploaded(
        val fileName: String,
        val userId: String,
        val timestamp: Instant = Instant.now()
    ) : DomainEvent()

    data class ChatMessageSent(
        val userId: String,
        val message: String,
        val useKnowledge: Boolean,
        val timestamp: Instant = Instant.now()
    ) : DomainEvent()

    data class ChatResponseGenerated(
        val userId: String,
        val response: String,
        val timestamp: Instant = Instant.now()
    ) : DomainEvent()
}