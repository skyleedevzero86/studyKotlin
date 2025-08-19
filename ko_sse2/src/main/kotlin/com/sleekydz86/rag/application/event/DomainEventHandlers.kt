package com.sleekydz86.rag.application.event

import com.sleekydz86.rag.domain.event.DomainEvent
import org.slf4j.LoggerFactory  // ← 올바른 import
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class DomainEventHandlers {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handle(event: DomainEvent.DocumentUploaded) {
        logger.info("문서 업로드 이벤트 처리: ${event.fileName} by ${event.userId}")
        // 추가 비즈니스 로직 (예: 알림, 로깅, 분석 등)
    }

    @EventListener
    @Async
    fun handle(event: DomainEvent.ChatMessageSent) {
        logger.info("채팅 메시지 이벤트 처리: ${event.userId} - ${event.useKnowledge}")
        // 사용자 행동 분석, 통계 수집 등
    }

    @EventListener
    @Async
    fun handle(event: DomainEvent.ChatResponseGenerated) {
        logger.info("채팅 응답 생성 이벤트 처리: ${event.userId}")
        // 응답 품질 분석, 성능 모니터링 등
    }
}