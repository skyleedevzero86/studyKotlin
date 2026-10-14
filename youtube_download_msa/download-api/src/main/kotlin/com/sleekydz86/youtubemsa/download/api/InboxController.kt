package com.sleekydz86.youtubemsa.download.api

import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import com.sleekydz86.youtubemsa.shared.InboxQuery
import com.sleekydz86.youtubemsa.shared.InboxState
import com.sleekydz86.youtubemsa.shared.requireConsumer
import com.sleekydz86.youtubemsa.shared.requireEventId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/inbox")
class InboxController(private val inbox: DeliveryInbox) {
    @PostMapping("/done-check")
    fun check(@RequestBody body: InboxQuery): InboxState =
        InboxState(inbox.alreadyDone(requireConsumer(body.consumer), requireEventId(body.eventId)))

    @PostMapping("/done")
    fun done(@RequestBody body: InboxQuery): ResponseEntity<Void> {
        inbox.markDone(requireConsumer(body.consumer), requireEventId(body.eventId))
        return ResponseEntity.noContent().build()
    }
}
