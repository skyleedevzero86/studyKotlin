package com.sleekydz86.cdcmigration.attachment.application

import com.sleekydz86.cdcmigration.attachment.domain.Attachment
import com.sleekydz86.cdcmigration.attachment.domain.FileTransfer
import java.time.Instant

data class AttachmentView(
    val id: Long,
    val subNo: Long,
    val requestId: String,
    val originInstCode: String?,
    val originFileId: String?,
    val originRegno: String?,
    val originSeq: Int?,
    val fileName: String,
    val contentType: String?,
    val byteSize: Long,
    val sha256: String,
    val bucket: String,
    val objectKey: String,
    val status: String,
    val statusLabel: String,
    val lastError: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    companion object {
        fun of(attachment: Attachment) = AttachmentView(
            attachment.id, attachment.subNo, attachment.requestId, attachment.origin?.instCode, attachment.origin?.fileId,
            attachment.origin?.regno, attachment.origin?.seq, attachment.fileName, attachment.contentType, attachment.fingerprint.size,
            attachment.fingerprint.sha256, attachment.location.bucket, attachment.location.key, attachment.status.name,
            attachment.status.label, attachment.lastError, attachment.createdAt, attachment.updatedAt,
        )
    }
}

data class AttachmentVerification(
    val attachmentId: Long,
    val metadataSize: Long,
    val metadataSha256: String,
    val databaseSize: Long?,
    val databaseSha256: String?,
    val storageSize: Long?,
    val storageSha256: String?,
    val storageError: String?,
    val matches: Boolean,
    val message: String,
)

data class FileTransferView(
    val transferId: Long,
    val source: String,
    val batchId: Long?,
    val cdcRunId: Long?,
    val instCode: String,
    val legacyFileId: String,
    val legacyRegno: String?,
    val seq: Int?,
    val fileName: String?,
    val sourceBucket: String,
    val sourceObjectKey: String,
    val status: String,
    val statusLabel: String,
    val attachmentId: Long?,
    val attempts: Int,
    val lastError: String?,
    val createdAt: Instant?,
    val completedAt: Instant?,
) {
    companion object {
        fun of(transfer: FileTransfer) = FileTransferView(
            transfer.transferId, transfer.source, transfer.batchId, transfer.cdcRunId, transfer.origin.instCode, transfer.origin.fileId,
            transfer.origin.regno, transfer.origin.seq, transfer.fileName, transfer.sourceLocation.bucket, transfer.sourceLocation.key,
            transfer.status.name, transfer.status.label, transfer.attachmentId, transfer.attempts, transfer.lastError,
            transfer.createdAt, transfer.completedAt,
        )
    }
}

data class FileTransferBatchResult(
    val batchId: Long,
    val total: Int,
    val done: Int,
    val unmapped: Int,
    val failed: Int,
    val items: List<FileTransferView>,
)
