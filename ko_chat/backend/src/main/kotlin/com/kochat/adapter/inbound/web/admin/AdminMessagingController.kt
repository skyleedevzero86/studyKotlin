package com.kochat.adapter.inbound.web.admin

import com.kochat.global.application.messaging.MessagingOperationsService
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "관리자 · 메시징 운영", description = "Outbox, DLQ, Kafka lag 모니터링")
@RestController
@RequestMapping("/api/v1/admin/messaging")
class AdminMessagingController(
    private val messagingOperationsService: MessagingOperationsService,
) {
    @Operation(summary = "메시징 운영 스냅샷 (Outbox, processed_events, DLQ, lag)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/operations")
    fun operations(): ResponseEntity<*> =
        ResponseEntity.ok(messagingOperationsService.getSnapshot())

    @Operation(summary = "실패 Outbox 이벤트 재큐잉")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/outbox/requeue-failed")
    fun requeueFailedOutbox(
        @RequestParam(defaultValue = "50") limit: Int,
    ): ResponseEntity<Map<String, Int>> =
        ResponseEntity.ok(
            mapOf("requeued" to messagingOperationsService.requeueFailedOutbox(limit)),
        )

    @Operation(summary = "DLQ 이벤트 원본 토픽으로 재발행")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/dlq/{dlqEventId}/replay")
    fun replayDlq(
        @PathVariable dlqEventId: Long,
    ): ResponseEntity<Map<String, Boolean>> =
        ResponseEntity.ok(
            mapOf("replayed" to messagingOperationsService.replayDlqEvent(dlqEventId)),
        )
}
