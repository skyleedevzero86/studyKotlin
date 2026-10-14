package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import com.sleekydz86.youtubemsa.shared.DownloadEventType
import com.sleekydz86.youtubemsa.shared.acceptDelivery
import com.sleekydz86.youtubemsa.shared.downloadEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class MediaChangeListener(
    private val ledger: MediaLedger,
    private val files: VideoFiles,
    private val remote: RemoteStore,
    private val inbox: DeliveryInbox,
    properties: MediaProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val workRoot = Path.of(properties.workDirectory)

    @KafkaListener(topics = ["\${study.cdc.topic}"])
    fun onEvent(message: String) {
        val event = downloadEvent(message)
        if (event == null) {
            log.warn("이벤트를 해석하지 못해 건너뜁니다.")
            return
        }
        try {
            val handled = acceptDelivery(inbox, "media", event.eventId) {
                if (event.eventType == DownloadEventType.DOWNLOAD_REQUESTED) {
                    fulfillDownload(event.downloadId, event.eventId, ledger, files, remote, workRoot)
                }
            }
            if (!handled) log.info("이미 처리한 이벤트라 건너뜁니다. eventId={}", event.eventId)
        } catch (ex: Exception) {
            log.error("미디어 이벤트 처리 실패 id={} eventId={}", event.downloadId, event.eventId, ex)
            throw ex
        }
    }
}
