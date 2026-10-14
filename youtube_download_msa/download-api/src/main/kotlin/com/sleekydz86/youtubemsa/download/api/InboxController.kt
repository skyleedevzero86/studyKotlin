package com.sleekydz86.youtubemsa.download.api

import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import com.sleekydz86.youtubemsa.shared.InboxQuery
import com.sleekydz86.youtubemsa.shared.InboxState
import com.sleekydz86.youtubemsa.shared.requireConsumer
import com.sleekydz86.youtubemsa.shared.requireEventId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "08-수신함")
@RestController
@RequestMapping("/internal/inbox")
class InboxController(private val inbox: DeliveryInbox) {
    @PostMapping("/done-check")
    @Operation(summary = "처리 여부 확인", description = "이 소비자가 해당 이벤트를 이미 처리했는지 확인합니다.")
    fun check(@RequestBody body: InboxQuery): InboxState =
        InboxState(inbox.alreadyDone(requireConsumer(body.consumer), requireEventId(body.eventId)))

    @PostMapping("/done")
    @Operation(summary = "처리 완료 기록", description = "이벤트 처리 완료를 남깁니다. 반영하면 본문 없이 204입니다.")
    fun done(@RequestBody body: InboxQuery): ResponseEntity<Void> {
        inbox.markDone(requireConsumer(body.consumer), requireEventId(body.eventId))
        return ResponseEntity.noContent().build()
    }
}
