package com.sleekydz86.cdcmigration.attachment.infrastructure

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.Instant

@Mapper
interface AttachmentMapper {
    fun findByRequestId(@Param("requestId") requestId: String): AttachmentRow?
    fun findById(@Param("id") id: Long): AttachmentRow?
    fun listBySubNo(@Param("subNo") subNo: Long): List<AttachmentRow>
    fun loadContent(@Param("id") id: Long): AttachmentContentRow?
    fun createBatch(row: BatchRow): Int
    fun findTransfer(@Param("id") id: Long): FileTransferRow?
    fun findTransfersByBatch(@Param("batchId") batchId: Long): List<FileTransferRow>
    fun findRetryableTransfers(@Param("limit") limit: Int): List<FileTransferRow>
    fun listTransfers(@Param("status") status: String?, @Param("limit") limit: Int): List<FileTransferRow>
}

data class AttachmentRow(
    var attachmentId: Long = 0,
    var subNo: Long = 0,
    var requestId: String = "",
    var originInstCode: String? = null,
    var originFileId: String? = null,
    var originRegno: String? = null,
    var originSeq: Int? = null,
    var fileName: String = "",
    var contentType: String? = null,
    var byteSize: Long = 0,
    var sha256: String = "",
    var bucket: String = "",
    var objectKey: String = "",
    var status: String = "",
    var lastError: String? = null,
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
)

class AttachmentContentRow {
    var fileContent: ByteArray? = null
}

data class BatchRow(var batchId: Long? = null, var total: Int = 0)

data class FileTransferRow(
    var transferId: Long = 0,
    var source: String = "",
    var batchId: Long? = null,
    var cdcRunId: Long? = null,
    var instCode: String = "",
    var legacyFileId: String = "",
    var legacyRegno: String? = null,
    var seq: Int? = null,
    var fileName: String? = null,
    var contentType: String? = null,
    var sourceBucket: String = "",
    var sourceObjectKey: String = "",
    var status: String = "",
    var attachmentId: Long? = null,
    var attempts: Int = 0,
    var lastError: String? = null,
    var createdAt: Instant? = null,
    var completedAt: Instant? = null,
)
