package com.sleekydz86.cdcmigration.attachment.domain

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("첨부파일 저장 상태")
class AttachmentTest {

    private fun attachment(status: AttachmentStatus = AttachmentStatus.PENDING) = Attachment(
        1, 8501, "req-1", null, "a.txt", "text/plain", FileFingerprint.of("abc".toByteArray()),
        StorageLocation("target-attachments", "submissions/8501/req-1/a.txt"), status, null, null, null,
    )

    @Test
    @DisplayName("MinIO 업로드와 DB 본문 저장이 모두 끝나야 READY가 된다")
    fun readyNeedsBothStores() {
        val attachment = attachment()

        assertThatThrownBy { attachment.markReady(objectStored = true, contentSaved = false) }
            .isInstanceOfSatisfying(BusinessException::class.java) { assertThat(it.errorCode).isEqualTo(ErrorCode.ATTACHMENT_STATE) }
        attachment.markReady(objectStored = true, contentSaved = true)
        assertThat(attachment.status).isEqualTo(AttachmentStatus.READY)
    }

    @Test
    @DisplayName("실패한 첨부파일은 재시도하면 PENDING으로 돌아간다")
    fun retryFromFailed() {
        val attachment = attachment()
        attachment.markFailed("DB 본문 저장 실패")

        attachment.retry()

        assertThat(attachment.status).isEqualTo(AttachmentStatus.PENDING)
        assertThat(attachment.lastError).isNull()
    }

    @Test
    @DisplayName("같은 요청 ID에 내용이 다른 파일이 오면 충돌이다")
    fun conflictOnDifferentFile() {
        val attachment = attachment(AttachmentStatus.READY)

        assertThatThrownBy { attachment.ensureSameRequest(8501, FileFingerprint.of("other".toByteArray())) }
            .isInstanceOfSatisfying(BusinessException::class.java) { assertThat(it.errorCode).isEqualTo(ErrorCode.REQUEST_ID_CONFLICT) }
    }
}
