package com.kochat.global.application.chat

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors

@Service
class LinkPreviewEnrichmentService(
    private val linkPreviewService: LinkPreviewService,
    private val chatMessageTxService: ChatMessageTxService,
    private val chatMessageDispatchService: ChatMessageDispatchService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newVirtualThreadPerTaskExecutor()

    fun scheduleEnrichment(messageId: Long, url: String) {
        executor.execute {
            try {
                val preview = linkPreviewService.preview(url)
                val updated = chatMessageTxService.updateLinkMetadata(messageId, preview) ?: return@execute
                chatMessageDispatchService.publishMessageUpdate(updated)
            } catch (e: Exception) {
                logger.debug("링크 미리보기 보강 실패 (messageId={}): {}", messageId, e.message)
            }
        }
    }
}
