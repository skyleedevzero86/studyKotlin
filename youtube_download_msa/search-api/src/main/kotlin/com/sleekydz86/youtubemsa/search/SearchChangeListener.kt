package com.sleekydz86.youtubemsa.search

import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import com.sleekydz86.youtubemsa.shared.StudyException
import com.sleekydz86.youtubemsa.shared.acceptDelivery
import com.sleekydz86.youtubemsa.shared.downloadEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SearchChangeListener(
    private val ledger: DownloadLedgerClient,
    private val catalog: DownloadCatalog,
    private val inbox: DeliveryInbox,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${study.cdc.topic}"])
    fun onEvent(message: String) {
        val event = downloadEvent(message)
        if (event == null) {
            log.warn("이벤트를 해석하지 못해 건너뜁니다.")
            return
        }
        try {
            val handled = acceptDelivery(inbox, "search", event.eventId) {
                val view = ledger.find(event.downloadId)
                    ?: throw StudyException("원장에 없는 변경입니다. 번호: ${event.downloadId}", status = 404)
                catalog.upsert(view)
            }
            if (!handled) log.info("이미 처리한 이벤트라 건너뜁니다. eventId={}", event.eventId)
        } catch (ex: Exception) {
            log.error("검색 이벤트 처리 실패 id={} eventId={}", event.downloadId, event.eventId, ex)
            throw ex
        }
    }
}
