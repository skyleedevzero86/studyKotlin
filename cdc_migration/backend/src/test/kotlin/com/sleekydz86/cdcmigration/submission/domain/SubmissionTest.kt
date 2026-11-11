package com.sleekydz86.cdcmigration.submission.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("심의 신청 상태 전이")
class SubmissionTest {

    private val updatedAt = Instant.parse("2026-11-01T00:00:00Z")

    private fun submission(status: SubmissionStatus) = Submission(
        8501, LegacySubmissionKey("H01", "1220"), "P-2026-00001", "U0000001", "제목", status, null, updatedAt, false, 0,
    )

    @Test
    @DisplayName("접수에서 승인으로 바로 바뀌는 구시스템 변경을 반영한다")
    fun receivedToApproved() {
        val submission = submission(SubmissionStatus.RECEIVED)

        val result = submission.applyLegacyChange(LegacySubmissionChange("제목", SubmissionStatus.APPROVED, "P-2026-00001", updatedAt.plusSeconds(60)))

        assertThat(result).isEqualTo(ChangeResult.Applied)
        assertThat(submission.status).isEqualTo(SubmissionStatus.APPROVED)
    }

    @Test
    @DisplayName("승인된 신청을 접수로 되돌리는 변경은 거부한다")
    fun approvedCannotGoBack() {
        val submission = submission(SubmissionStatus.APPROVED)

        val result = submission.applyLegacyChange(LegacySubmissionChange("제목", SubmissionStatus.RECEIVED, "P-2026-00001", updatedAt.plusSeconds(60)))

        assertThat(result).isInstanceOf(ChangeResult.Rejected::class.java)
        assertThat((result as ChangeResult.Rejected).reason).contains("승인에서 접수")
        assertThat(submission.status).isEqualTo(SubmissionStatus.APPROVED)
    }

    @Test
    @DisplayName("이미 반영된 변경보다 오래된 이벤트는 무시한다")
    fun ignoresOlderChange() {
        val submission = submission(SubmissionStatus.RECEIVED)

        val result = submission.applyLegacyChange(LegacySubmissionChange("제목", SubmissionStatus.APPROVED, "P-2026-00001", updatedAt.minusSeconds(60)))

        assertThat(result).isInstanceOf(ChangeResult.Ignored::class.java)
        assertThat(submission.status).isEqualTo(SubmissionStatus.RECEIVED)
    }

    @Test
    @DisplayName("구시스템 삭제는 행을 지우지 않고 삭제 표시만 한다")
    fun deleteMarksOnly() {
        val submission = submission(SubmissionStatus.UNDER_REVIEW)

        val first = submission.markDeleted(null)
        val second = submission.markDeleted(null)

        assertThat(first).isEqualTo(ChangeResult.Applied)
        assertThat(second).isInstanceOf(ChangeResult.Ignored::class.java)
        assertThat(submission.deleted).isTrue()
    }
}
