package com.sleekydz86.cdcmigration.attachment.domain

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.global.util.Hashing
import java.time.Instant

enum class AttachmentStatus(val label: String) {
    PENDING("저장 중"),
    READY("저장 완료"),
    FAILED("저장 실패"),
}

data class StorageLocation(val bucket: String, val key: String) {
    override fun toString(): String = "$bucket/$key"
}

data class FileFingerprint(val size: Long, val sha256: String) {
    companion object {
        fun of(bytes: ByteArray) = FileFingerprint(bytes.size.toLong(), Hashing.sha256Hex(bytes))
    }
}

data class LegacyFileOrigin(val instCode: String, val fileId: String, val regno: String?, val seq: Int?)

data class NewAttachment(
    val subNo: Long,
    val requestId: String,
    val origin: LegacyFileOrigin?,
    val fileName: String,
    val contentType: String?,
    val fingerprint: FileFingerprint,
    val location: StorageLocation,
)

class Attachment(
    val id: Long,
    val subNo: Long,
    val requestId: String,
    val origin: LegacyFileOrigin?,
    val fileName: String,
    val contentType: String?,
    val fingerprint: FileFingerprint,
    val location: StorageLocation,
    status: AttachmentStatus,
    lastError: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    var status: AttachmentStatus = status
        private set
    var lastError: String? = lastError
        private set

    fun ensureSameRequest(subNo: Long, fingerprint: FileFingerprint) {
        if (this.subNo != subNo || this.fingerprint != fingerprint) {
            throw BusinessException(
                ErrorCode.REQUEST_ID_CONFLICT,
                "요청 ID $requestId 는 이미 신청 ${this.subNo}의 '$fileName'(${this.fingerprint.size}바이트) 업로드에 쓰였습니다.",
            )
        }
    }

    fun retry() {
        if (status == AttachmentStatus.FAILED) {
            status = AttachmentStatus.PENDING
            lastError = null
        }
    }

    fun markReady(objectStored: Boolean, contentSaved: Boolean) {
        if (!objectStored || !contentSaved) {
            throw BusinessException(ErrorCode.ATTACHMENT_STATE, "MinIO 업로드와 DB 본문 저장이 모두 끝나야 저장 완료로 바꿀 수 있습니다.")
        }
        status = AttachmentStatus.READY
        lastError = null
    }

    fun markFailed(reason: String) {
        if (status == AttachmentStatus.READY) {
            throw BusinessException(ErrorCode.ATTACHMENT_STATE, "이미 저장 완료된 첨부파일은 실패로 바꿀 수 없습니다.")
        }
        status = AttachmentStatus.FAILED
        lastError = reason
    }
}
