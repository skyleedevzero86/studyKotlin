package com.sleekydz86.cdcmigration.attachment.application

import com.sleekydz86.cdcmigration.attachment.domain.Attachment
import com.sleekydz86.cdcmigration.attachment.domain.AttachmentStatus
import com.sleekydz86.cdcmigration.attachment.domain.FileFingerprint
import com.sleekydz86.cdcmigration.attachment.domain.LegacyFileOrigin
import com.sleekydz86.cdcmigration.attachment.domain.NewAttachment
import com.sleekydz86.cdcmigration.attachment.domain.StorageLocation
import com.sleekydz86.cdcmigration.global.config.AppProperties
import com.sleekydz86.cdcmigration.global.demo.DemoFaults
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

data class UploadCommand(
    val subNo: Long,
    val requestId: String,
    val fileName: String,
    val contentType: String?,
    val bytes: ByteArray,
    val origin: LegacyFileOrigin? = null,
)

@Service
class AttachmentUploadService(
    private val attachments: AttachmentRepository,
    private val storage: ObjectStorage,
    private val submissions: SubmissionLookup,
    private val faults: DemoFaults,
    private val properties: AppProperties,
    transactionManager: PlatformTransactionManager,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val transaction = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    fun upload(command: UploadCommand): Attachment {
        val requestId = command.requestId.trim()
        if (requestId.isEmpty()) {
            throw BusinessException(ErrorCode.REQUEST_ID_REQUIRED)
        }
        if (command.bytes.isEmpty()) {
            throw BusinessException(ErrorCode.FILE_EMPTY, command.fileName)
        }
        val limit = properties.attachment.maxFileBytes
        if (command.bytes.size > limit) {
            throw BusinessException(ErrorCode.FILE_TOO_LARGE, "${command.fileName}: ${command.bytes.size}바이트 > 상한 ${limit}바이트")
        }
        if (!submissions.exists(command.subNo)) {
            throw BusinessException(ErrorCode.SUBMISSION_NOT_FOUND, command.subNo.toString())
        }
        val fingerprint = FileFingerprint.of(command.bytes)
        val attachment = transaction.execute { prepare(command, requestId, fingerprint) }!!
        if (attachment.status == AttachmentStatus.READY) {
            log.info("요청 ID {} 재전송: 이미 저장 완료된 첨부파일 {}를 돌려줍니다.", requestId, attachment.id)
            return attachment
        }
        try {
            storage.put(attachment.location, command.bytes, command.contentType)
        } catch (exception: Exception) {
            fail(attachment, "MinIO 업로드 실패: ${exception.message}")
            throw if (exception is BusinessException) exception else BusinessException(ErrorCode.STORAGE_FAILED, exception.message, exception)
        }
        try {
            transaction.execute {
                attachments.saveContent(attachment.id, command.bytes)
                if (faults.consumeContentSaveFailure()) {
                    throw IllegalStateException("실습용 장애 주입: MinIO 업로드 후 DB 본문 저장을 실패시켰습니다.")
                }
                attachment.markReady(objectStored = true, contentSaved = true)
                attachments.updateStatus(attachment)
            }
        } catch (exception: Exception) {
            fail(attachment, "DB 본문 저장 실패: ${exception.message}")
            throw BusinessException(
                ErrorCode.CONTENT_SAVE_FAILED,
                "요청 ID $requestId, 객체 키 ${attachment.location}. 원인: ${exception.message}",
                exception,
            )
        }
        log.info("첨부파일 {} 저장 완료: 신청 {}, {}, {}바이트", attachment.id, attachment.subNo, attachment.location, fingerprint.size)
        return attachment
    }

    private fun prepare(command: UploadCommand, requestId: String, fingerprint: FileFingerprint): Attachment {
        val existing = attachments.findByRequestId(requestId)
        if (existing != null) {
            existing.ensureSameRequest(command.subNo, fingerprint)
            if (existing.status != AttachmentStatus.READY) {
                existing.retry()
                attachments.updateStatus(existing)
                log.info("요청 ID {} 재시도: 첨부파일 {}를 같은 객체 키 {}로 다시 저장합니다.", requestId, existing.id, existing.location)
            }
            return existing
        }
        val id = attachments.create(
            NewAttachment(
                command.subNo, requestId, command.origin, command.fileName, command.contentType, fingerprint,
                StorageLocation(properties.attachment.bucket, objectKey(command.subNo, requestId, command.fileName)),
            ),
        )
        return attachments.findById(id)!!
    }

    private fun fail(attachment: Attachment, reason: String) {
        runCatching {
            transaction.execute {
                attachment.markFailed(reason.take(1000))
                attachments.updateStatus(attachment)
            }
        }.onFailure { log.error("첨부파일 {} 실패 상태를 기록하지 못했습니다: {}", attachment.id, it.message) }
        log.warn("첨부파일 {} 저장 실패: {}", attachment.id, reason)
    }

    private fun objectKey(subNo: Long, requestId: String, fileName: String): String =
        "submissions/$subNo/${safe(requestId)}/${safe(fileName)}"

    private fun safe(value: String): String = value.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").trim().ifEmpty { "file" }
}
