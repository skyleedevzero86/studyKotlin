package com.sleekydz86.cdcmigration.attachment.application

import com.sleekydz86.cdcmigration.attachment.domain.FileTransfer
import com.sleekydz86.cdcmigration.attachment.domain.FileTransferRequest
import com.sleekydz86.cdcmigration.attachment.domain.LegacyFileOrigin
import com.sleekydz86.cdcmigration.attachment.domain.StorageLocation
import com.sleekydz86.cdcmigration.attachment.domain.TransferStatus
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.migration.application.FileTransferRequester
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

@Service
class FileTransferService(
    private val transfers: FileTransferRepository,
    private val submissions: SubmissionLookup,
    private val storage: ObjectStorage,
    private val uploads: AttachmentUploadService,
    transactionManager: PlatformTransactionManager,
) : FileTransferRequester {

    private val log = LoggerFactory.getLogger(javaClass)
    private val transaction = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    fun importCsv(rows: List<Map<String, String?>>): FileTransferBatchResult {
        if (rows.isEmpty()) {
            throw BusinessException(ErrorCode.CSV_FORMAT, "첨부파일 CSV에 데이터 행이 없습니다.")
        }
        var transferIds = emptyList<Long>()
        val batchId = transaction.execute {
            val id = transfers.createBatch(rows.size)
            transferIds = rows.mapIndexed { index, row -> transfers.request(toRequest(row, "CSV", id, null, index + 1)).transferId }
            id
        }!!
        transferIds.mapNotNull { transfers.find(it) }.filter { it.status != TransferStatus.DONE }.forEach { process(it) }
        val items = transferIds.mapNotNull { transfers.find(it) }
        log.info("첨부파일 이관 묶음 {} 처리: 전체 {}건", batchId, items.size)
        return FileTransferBatchResult(
            batchId, items.size, items.count { it.status == TransferStatus.DONE }, items.count { it.status == TransferStatus.UNMAPPED },
            items.count { it.status == TransferStatus.FAILED }, items.map { FileTransferView.of(it) },
        )
    }

    override fun requestFromCdc(cdcRunId: Long, row: Map<String, String?>): Boolean =
        transfers.request(toRequest(row, "CDC", null, cdcRunId, null)).created

    fun processRetryable(): Int {
        val items = transfers.findRetryable(100)
        items.forEach { process(it) }
        return items.size
    }

    fun list(status: String?, limit: Int): List<FileTransferView> =
        transfers.list(status?.ifBlank { null }, limit.coerceIn(1, 1000)).map { FileTransferView.of(it) }

    fun process(transfer: FileTransfer) {
        val regno = transfer.origin.regno
        val subNo = regno?.let { submissions.findSubNo(transfer.origin.instCode, it) }
        if (subNo == null) {
            update(
                transfer, TransferStatus.UNMAPPED, null,
                "새 시스템에서 신청 ${transfer.origin.instCode}/${regno ?: "-"}을(를) 찾지 못했습니다. 신청이 이관되면 자동으로 다시 시도합니다.",
            )
            return
        }
        val bytes = try {
            storage.get(transfer.sourceLocation)
        } catch (exception: Exception) {
            update(transfer, TransferStatus.FAILED, null, "구 MinIO에서 파일을 읽지 못했습니다 (${transfer.sourceLocation}): ${exception.message}")
            return
        }
        try {
            val attachment = uploads.upload(
                UploadCommand(subNo, transfer.requestId, transfer.fileName ?: transfer.sourceLocation.key.substringAfterLast('/'),
                    transfer.contentType, bytes, transfer.origin),
            )
            update(transfer, TransferStatus.DONE, attachment.id, "신청 $subNo 에 첨부파일 ${attachment.id} 저장 (${bytes.size}바이트)")
        } catch (exception: BusinessException) {
            update(transfer, TransferStatus.FAILED, null, exception.message ?: exception.errorCode.message)
        }
    }

    private fun update(transfer: FileTransfer, status: TransferStatus, attachmentId: Long?, message: String) {
        transaction.execute { transfers.update(transfer.transferId, status, attachmentId, message.take(1000)) }
        if (status != TransferStatus.DONE) {
            log.info("첨부파일 이관 {} {}: {}", transfer.transferId, status.label, message)
        }
    }

    private fun toRequest(row: Map<String, String?>, source: String, batchId: Long?, cdcRunId: Long?, rowNo: Int?): FileTransferRequest {
        val position = rowNo?.let { "${it}행: " } ?: ""
        val inst = row.text("inst_code") ?: throw BusinessException(ErrorCode.CSV_FORMAT, "${position}기관 코드(inst_code)가 비어 있습니다.")
        val fileId = row.text("file_id") ?: throw BusinessException(ErrorCode.CSV_FORMAT, "${position}파일 ID(file_id)가 비어 있습니다.")
        val bucket = row.text("bucket") ?: throw BusinessException(ErrorCode.CSV_FORMAT, "${position}버킷(bucket)이 비어 있습니다.")
        val key = row.text("object_key") ?: throw BusinessException(ErrorCode.CSV_FORMAT, "${position}객체 키(object_key)가 비어 있습니다.")
        return FileTransferRequest(
            source, batchId, cdcRunId,
            LegacyFileOrigin(inst, fileId, row.text("regno"), row.text("seq")?.toIntOrNull()),
            row.text("file_name"), row.text("content_type"), StorageLocation(bucket, key),
        )
    }

    private fun Map<String, String?>.text(column: String): String? = this[column]?.trim()?.takeIf { it.isNotEmpty() }

    companion object {
        val CSV_COLUMNS = listOf("inst_code", "file_id", "regno", "seq", "file_name", "content_type", "bucket", "object_key")
    }
}
