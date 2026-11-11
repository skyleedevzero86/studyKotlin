package com.sleekydz86.cdcmigration.attachment.application

import com.sleekydz86.cdcmigration.attachment.domain.Attachment
import com.sleekydz86.cdcmigration.attachment.domain.FileTransfer
import com.sleekydz86.cdcmigration.attachment.domain.FileTransferRequest
import com.sleekydz86.cdcmigration.attachment.domain.NewAttachment
import com.sleekydz86.cdcmigration.attachment.domain.StorageLocation
import com.sleekydz86.cdcmigration.attachment.domain.TransferStatus

interface ObjectStorage {
    fun put(location: StorageLocation, bytes: ByteArray, contentType: String?)
    fun get(location: StorageLocation): ByteArray
}

interface AttachmentRepository {
    fun findByRequestId(requestId: String): Attachment?
    fun findById(id: Long): Attachment?
    fun create(attachment: NewAttachment): Long
    fun updateStatus(attachment: Attachment)
    fun saveContent(attachmentId: Long, bytes: ByteArray)
    fun loadContent(attachmentId: Long): ByteArray?
    fun listBySubmission(subNo: Long): List<Attachment>
}

interface FileTransferRepository {
    fun createBatch(total: Int): Long
    fun request(request: FileTransferRequest): TransferRequestResult
    fun find(transferId: Long): FileTransfer?
    fun findByBatch(batchId: Long): List<FileTransfer>
    fun findRetryable(limit: Int): List<FileTransfer>
    fun list(status: String?, limit: Int): List<FileTransfer>
    fun update(transferId: Long, status: TransferStatus, attachmentId: Long?, message: String?)
}

data class TransferRequestResult(val transferId: Long, val created: Boolean)

interface SubmissionLookup {
    fun findSubNo(instCode: String, regno: String): Long?
    fun exists(subNo: Long): Boolean
}
