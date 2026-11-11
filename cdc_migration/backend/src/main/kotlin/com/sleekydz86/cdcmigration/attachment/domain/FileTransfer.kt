package com.sleekydz86.cdcmigration.attachment.domain

import java.time.Instant

enum class TransferStatus(val label: String) {
    PENDING("대기"),
    DONE("완료"),
    UNMAPPED("신청 매핑 실패"),
    FAILED("실패"),
}

data class FileTransferRequest(
    val source: String,
    val batchId: Long?,
    val cdcRunId: Long?,
    val origin: LegacyFileOrigin,
    val fileName: String?,
    val contentType: String?,
    val sourceLocation: StorageLocation,
)

data class FileTransfer(
    val transferId: Long,
    val source: String,
    val batchId: Long?,
    val cdcRunId: Long?,
    val origin: LegacyFileOrigin,
    val fileName: String?,
    val contentType: String?,
    val sourceLocation: StorageLocation,
    val status: TransferStatus,
    val attachmentId: Long?,
    val attempts: Int,
    val lastError: String?,
    val createdAt: Instant?,
    val completedAt: Instant?,
) {
    val requestId: String get() = "MIG-${origin.instCode}-${origin.fileId}"
}
