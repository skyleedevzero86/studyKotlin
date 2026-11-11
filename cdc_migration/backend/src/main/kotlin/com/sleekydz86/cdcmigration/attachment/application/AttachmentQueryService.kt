package com.sleekydz86.cdcmigration.attachment.application

import com.sleekydz86.cdcmigration.attachment.domain.Attachment
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.global.util.Hashing
import org.springframework.stereotype.Service

@Service
class AttachmentQueryService(
    private val attachments: AttachmentRepository,
    private val storage: ObjectStorage,
    private val submissions: SubmissionLookup,
) {

    fun list(subNo: Long): List<AttachmentView> {
        if (!submissions.exists(subNo)) {
            throw BusinessException(ErrorCode.SUBMISSION_NOT_FOUND, subNo.toString())
        }
        return attachments.listBySubmission(subNo).map { AttachmentView.of(it) }
    }

    fun get(id: Long): AttachmentView = AttachmentView.of(load(id))

    fun download(id: Long): Pair<AttachmentView, ByteArray> {
        val attachment = load(id)
        return AttachmentView.of(attachment) to storage.get(attachment.location)
    }

    fun verify(id: Long): AttachmentVerification {
        val attachment = load(id)
        val database = attachments.loadContent(id)
        val stored = runCatching { storage.get(attachment.location) }
        val storageBytes = stored.getOrNull()
        val databaseSha = database?.let { Hashing.sha256Hex(it) }
        val storageSha = storageBytes?.let { Hashing.sha256Hex(it) }
        val matches = database != null && storageBytes != null &&
            database.size.toLong() == attachment.fingerprint.size && storageBytes.size.toLong() == attachment.fingerprint.size &&
            databaseSha == attachment.fingerprint.sha256 && storageSha == attachment.fingerprint.sha256
        val message = when {
            database == null -> "DB에 본문(BYTEA)이 없습니다."
            storageBytes == null -> "MinIO에서 원본을 읽지 못했습니다: ${stored.exceptionOrNull()?.message}"
            matches -> "DB 본문과 MinIO 원본의 크기·SHA-256이 모두 같습니다."
            else -> "DB 본문과 MinIO 원본이 다릅니다."
        }
        return AttachmentVerification(
            id, attachment.fingerprint.size, attachment.fingerprint.sha256, database?.size?.toLong(), databaseSha,
            storageBytes?.size?.toLong(), storageSha, stored.exceptionOrNull()?.message, matches, message,
        )
    }

    private fun load(id: Long): Attachment =
        attachments.findById(id) ?: throw BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND, id.toString())
}
