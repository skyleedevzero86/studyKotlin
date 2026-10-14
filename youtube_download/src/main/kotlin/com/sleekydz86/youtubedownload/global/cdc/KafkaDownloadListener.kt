package com.sleekydz86.youtubedownload.global.cdc

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class KafkaDownloadListener(
    private val handler: DownloadChangeHandler,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${study.cdc.topic}"])
    fun onChange(message: String, acknowledgment: Acknowledgment) {
        val change = downloadChange(message)
        if (change == null) {
            log.warn("CDC 메시지를 해석하지 못해 건너뜁니다.")
            acknowledgment.acknowledge()
            return
        }
        try {
            handler.handle(change)
            acknowledgment.acknowledge()
        } catch (ex: Exception) {
            log.error("CDC 처리 실패 id={}", change.id, ex)
            throw ex
        }
    }
}
