package com.sleekydz86.cdcmigration.attachment.infrastructure

import com.sleekydz86.cdcmigration.attachment.application.AttachmentRepository
import com.sleekydz86.cdcmigration.attachment.application.FileTransferRepository
import com.sleekydz86.cdcmigration.attachment.application.SubmissionLookup
import com.sleekydz86.cdcmigration.attachment.application.TransferRequestResult
import com.sleekydz86.cdcmigration.attachment.domain.Attachment
import com.sleekydz86.cdcmigration.attachment.domain.AttachmentStatus
import com.sleekydz86.cdcmigration.attachment.domain.FileFingerprint
import com.sleekydz86.cdcmigration.attachment.domain.FileTransfer
import com.sleekydz86.cdcmigration.attachment.domain.FileTransferRequest
import com.sleekydz86.cdcmigration.attachment.domain.LegacyFileOrigin
import com.sleekydz86.cdcmigration.attachment.domain.NewAttachment
import com.sleekydz86.cdcmigration.attachment.domain.StorageLocation
import com.sleekydz86.cdcmigration.attachment.domain.TransferStatus
import com.sleekydz86.cdcmigration.global.procedure.AttachmentContentCall
import com.sleekydz86.cdcmigration.global.procedure.AttachmentCudCall
import com.sleekydz86.cdcmigration.global.procedure.CudProcedureMapper
import com.sleekydz86.cdcmigration.global.procedure.FileTransferCudCall
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import com.sleekydz86.cdcmigration.submission.application.SubmissionRepository
import com.sleekydz86.cdcmigration.submission.domain.LegacySubmissionKey
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
class MyBatisAttachmentRepository(
    private val mapper: AttachmentMapper,
    private val procedures: CudProcedureMapper,
) : AttachmentRepository {

    override fun findByRequestId(requestId: String): Attachment? = mapper.findByRequestId(requestId)?.toDomain()

    override fun findById(id: Long): Attachment? = mapper.findById(id)?.toDomain()

    override fun create(attachment: NewAttachment): Long {
        val worker = WorkerContext.current()
        val call = AttachmentCudCall(
            "C", worker.name, worker.ip, null, attachment.subNo, attachment.requestId, attachment.origin?.instCode,
            attachment.origin?.fileId, attachment.origin?.regno, attachment.origin?.seq, attachment.fileName, attachment.contentType,
            attachment.fingerprint.size, attachment.fingerprint.sha256, attachment.location.bucket, attachment.location.key, null, null,
        )
        procedures.attachmentCud(call)
        call.verify()
        return call.outAttachmentId!!
    }

    override fun updateStatus(attachment: Attachment) {
        val worker = WorkerContext.current()
        val call = AttachmentCudCall(
            "U", worker.name, worker.ip, attachment.id, null, null, null, null, null, null, attachment.fileName, null, null, null,
            null, null, attachment.status.name, attachment.lastError,
        )
        procedures.attachmentCud(call)
        call.verify()
    }

    override fun saveContent(attachmentId: Long, bytes: ByteArray) {
        val worker = WorkerContext.current()
        val call = AttachmentContentCall(worker.name, worker.ip, attachmentId, bytes)
        procedures.attachmentContent(call)
        call.verify()
    }

    override fun loadContent(attachmentId: Long): ByteArray? = mapper.loadContent(attachmentId)?.fileContent

    override fun listBySubmission(subNo: Long): List<Attachment> = mapper.listBySubNo(subNo).map { it.toDomain() }

    private fun AttachmentRow.toDomain() = Attachment(
        id = attachmentId,
        subNo = subNo,
        requestId = requestId,
        origin = if (originInstCode != null && originFileId != null) LegacyFileOrigin(originInstCode!!, originFileId!!, originRegno, originSeq) else null,
        fileName = fileName,
        contentType = contentType,
        fingerprint = FileFingerprint(byteSize, sha256.trim()),
        location = StorageLocation(bucket, objectKey),
        status = AttachmentStatus.valueOf(status),
        lastError = lastError,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

@Repository
class MyBatisFileTransferRepository(
    private val mapper: AttachmentMapper,
    private val procedures: CudProcedureMapper,
) : FileTransferRepository {

    override fun createBatch(total: Int): Long {
        val row = BatchRow(total = total)
        mapper.createBatch(row)
        return row.batchId!!
    }

    override fun request(request: FileTransferRequest): TransferRequestResult {
        val worker = WorkerContext.current()
        val call = FileTransferCudCall(
            "C", worker.name, worker.ip, null, request.source, request.batchId, request.cdcRunId, request.origin.instCode,
            request.origin.fileId, request.origin.regno, request.origin.seq, request.fileName, request.contentType,
            request.sourceLocation.bucket, request.sourceLocation.key, null, null, null,
        )
        procedures.fileTransferCud(call)
        call.verify()
        return TransferRequestResult(call.outTransferId!!, call.outCreated ?: false)
    }

    override fun find(transferId: Long): FileTransfer? = mapper.findTransfer(transferId)?.toDomain()

    override fun findByBatch(batchId: Long): List<FileTransfer> = mapper.findTransfersByBatch(batchId).map { it.toDomain() }

    override fun findRetryable(limit: Int): List<FileTransfer> = mapper.findRetryableTransfers(limit).map { it.toDomain() }

    override fun list(status: String?, limit: Int): List<FileTransfer> = mapper.listTransfers(status, limit).map { it.toDomain() }

    override fun update(transferId: Long, status: TransferStatus, attachmentId: Long?, message: String?) {
        val worker = WorkerContext.current()
        val call = FileTransferCudCall(
            "U", worker.name, worker.ip, transferId, null, null, null, null, null, null, null, null, null, null, null,
            status.name, attachmentId, message,
        )
        procedures.fileTransferCud(call)
        call.verify()
    }

    private fun FileTransferRow.toDomain() = FileTransfer(
        transferId = transferId,
        source = source,
        batchId = batchId,
        cdcRunId = cdcRunId,
        origin = LegacyFileOrigin(instCode, legacyFileId, legacyRegno, seq),
        fileName = fileName,
        contentType = contentType,
        sourceLocation = StorageLocation(sourceBucket, sourceObjectKey),
        status = TransferStatus.valueOf(status),
        attachmentId = attachmentId,
        attempts = attempts,
        lastError = lastError,
        createdAt = createdAt,
        completedAt = completedAt,
    )
}

@Component
class SubmissionLookupAdapter(private val submissions: SubmissionRepository) : SubmissionLookup {

    override fun findSubNo(instCode: String, regno: String): Long? =
        submissions.findByKey(LegacySubmissionKey(instCode, regno))?.takeUnless { it.deleted }?.subNo

    override fun exists(subNo: Long): Boolean = submissions.findBySubNo(subNo) != null
}
