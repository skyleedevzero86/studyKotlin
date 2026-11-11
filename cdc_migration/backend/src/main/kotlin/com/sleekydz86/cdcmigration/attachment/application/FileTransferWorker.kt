package com.sleekydz86.cdcmigration.attachment.application

import com.sleekydz86.cdcmigration.global.procedure.Worker
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FileTransferWorker(private val transfers: FileTransferService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${app.attachment.worker-interval-ms:3000}")
    fun run() {
        runCatching { WorkerContext.runAs(Worker.BATCH) { transfers.processRetryable() } }
            .onFailure { log.warn("첨부파일 이관 작업 처리 중 오류: {}", it.message) }
    }
}
