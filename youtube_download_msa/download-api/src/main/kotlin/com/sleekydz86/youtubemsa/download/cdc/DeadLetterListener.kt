package com.sleekydz86.youtubemsa.download.cdc

import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import com.sleekydz86.youtubemsa.shared.downloadEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class DeadLetterListener(
    private val inbox: DeliveryInbox,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${study.cdc.dead-letter-topic}"])
    fun onDeadLetter(message: String) {
        val event = downloadEvent(message)
        if (event == null) {
            log.error("DLQ 본문을 해석하지 못했습니다.")
            return
        }
        if (inbox.alreadyDone("dlq", event.eventId)) return
        log.error(
            "재시도를 끝내고 DLQ에 적재했습니다. eventId={} downloadId={} type={}",
            event.eventId,
            event.downloadId,
            event.eventType.name,
        )
        inbox.markDone("dlq", event.eventId)
    }
}
