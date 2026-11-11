package com.sleekydz86.cdcmigration.migration.application.cdc

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.migration.application.FileTransferRequester
import com.sleekydz86.cdcmigration.migration.application.MappingRepository
import com.sleekydz86.cdcmigration.migration.domain.cdc.ApplyOutcome
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRun
import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeOperation
import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import com.sleekydz86.cdcmigration.migration.domain.cdc.StoredCdcEvent
import com.sleekydz86.cdcmigration.submission.application.SubmissionRepository
import com.sleekydz86.cdcmigration.submission.domain.ChangeResult
import com.sleekydz86.cdcmigration.submission.domain.LegacySubmissionChange
import com.sleekydz86.cdcmigration.submission.domain.LegacySubmissionKey
import com.sleekydz86.cdcmigration.submission.domain.NewSubmission
import com.sleekydz86.cdcmigration.submission.domain.ReviewResult
import com.sleekydz86.cdcmigration.submission.domain.ReviewUpsert
import com.sleekydz86.cdcmigration.submission.domain.SubmissionStatus
import org.springframework.stereotype.Component

@Component
class CdcChangeApplier(
    private val mappings: MappingRepository,
    private val submissions: SubmissionRepository,
    private val fileTransfers: FileTransferRequester,
) {

    fun apply(run: CdcRun, event: StoredCdcEvent): ApplyOutcome = try {
        when (event.table) {
            LegacyTable.USER -> applyUser(run, event)
            LegacyTable.PROJECT -> applyProject(run, event)
            LegacyTable.SUBMISSION -> applySubmission(run, event)
            LegacyTable.REVIEW -> applyReview(run, event)
            LegacyTable.ATTACHMENT -> applyAttachment(run, event)
        }
    } catch (exception: BusinessException) {
        ApplyOutcome.failed(exception.message ?: exception.errorCode.message)
    }

    private fun applyUser(run: CdcRun, event: StoredCdcEvent): ApplyOutcome {
        val row = event.row
        val inst = row.text("inst_code") ?: return ApplyOutcome.failed("필수값 누락: 기관 코드")
        val legacyId = row.text("user_id") ?: return ApplyOutcome.failed("필수값 누락: 사용자 ID")
        if (event.op == ChangeOperation.DELETE) {
            val userId = mappings.findUserId(inst, legacyId) ?: return ApplyOutcome.skipped("새 시스템에 없는 사용자 $inst/$legacyId")
            mappings.deactivateUser(userId)
            return ApplyOutcome.applied("사용자 $inst/$legacyId($userId) 사용 중지")
        }
        val name = row.text("user_name") ?: return ApplyOutcome.failed("필수값 누락: 사용자 이름 ($inst/$legacyId)")
        val userId = mappings.ensureUser(inst, legacyId, run.migrationRunId)
        mappings.upsertUser(userId, inst, name, row.text("email"), row.text("dept_name"))
        return ApplyOutcome.applied("사용자 $inst/$legacyId → $userId ${event.op.label}")
    }

    private fun applyProject(run: CdcRun, event: StoredCdcEvent): ApplyOutcome {
        val row = event.row
        val inst = row.text("inst_code") ?: return ApplyOutcome.failed("필수값 누락: 기관 코드")
        val researchNo = row.text("research_no") ?: return ApplyOutcome.failed("필수값 누락: 연구번호")
        if (event.op == ChangeOperation.DELETE) {
            val projectNo = mappings.findProjectNo(inst, researchNo) ?: return ApplyOutcome.skipped("새 시스템에 없는 과제 $inst/$researchNo")
            mappings.deactivateProject(projectNo)
            return ApplyOutcome.applied("과제 $inst/$researchNo($projectNo) 사용 중지")
        }
        val title = row.text("title") ?: return ApplyOutcome.failed("필수값 누락: 과제명 ($inst/$researchNo)")
        val piLegacy = row.text("pi_user_id")
        val piUserId = piLegacy?.let { mappings.findUserId(inst, it) ?: return ApplyOutcome.waiting("책임연구자 $inst/$it 매핑 대기") }
        val projectNo = mappings.ensureProject(inst, researchNo, run.migrationRunId)
        mappings.upsertProject(projectNo, inst, title, piUserId, mappings.toDate(row["start_date"]), mappings.toDate(row["end_date"]))
        return ApplyOutcome.applied("과제 $inst/$researchNo → $projectNo ${event.op.label}")
    }

    private fun applySubmission(run: CdcRun, event: StoredCdcEvent): ApplyOutcome {
        val row = event.row
        val inst = row.text("inst_code") ?: return ApplyOutcome.failed("필수값 누락: 기관 코드")
        val regno = row.text("regno") ?: return ApplyOutcome.failed("필수값 누락: 신청번호")
        val key = LegacySubmissionKey(inst, regno)
        if (event.op == ChangeOperation.DELETE) {
            val existing = submissions.findByKey(key) ?: return ApplyOutcome.skipped("새 시스템에 없는 신청 $key 의 삭제 이벤트")
            return when (val result = existing.markDeleted(mappings.toTimestamp(row["updated_at"]))) {
                ChangeResult.Applied -> {
                    submissions.update(existing)
                    ApplyOutcome.applied("신청 $key (새 신청번호 ${existing.subNo}) 삭제 처리")
                }
                is ChangeResult.Ignored -> ApplyOutcome.skipped(result.reason)
                is ChangeResult.Rejected -> ApplyOutcome.failed(result.reason)
            }
        }
        val missing = listOfNotNull(
            "신청 제목".takeIf { row.text("title") == null },
            "신청자".takeIf { row.text("applicant_id") == null },
            "상태 코드".takeIf { row.text("status_code") == null },
        )
        if (missing.isNotEmpty()) {
            return ApplyOutcome.failed("필수값 누락: ${missing.joinToString(", ")} ($key)")
        }
        val statusCode = row.text("status_code")!!
        val status = mappings.mapCode("APPL_STATUS", inst, statusCode)?.let { SubmissionStatus.valueOf(it) }
            ?: return ApplyOutcome.failed("상태 코드 $statusCode 에 대응하는 새 상태가 없습니다 ($key)")
        val applicantLegacy = row.text("applicant_id")!!
        val applicant = mappings.findUserId(inst, applicantLegacy)
            ?: return ApplyOutcome.waiting("신청자 $inst/$applicantLegacy 매핑 대기 (사용자 변경이 아직 반영되지 않음)")
        val researchNo = row.text("research_no")
        val projectNo = if (researchNo == null) {
            if (!mappings.virtualRuleExists(inst, regno)) {
                return ApplyOutcome.failed("구시스템부터 과제 연결이 없고, 가상 과제 처리 기준도 없습니다 ($key)")
            }
            mappings.ensureVirtualProject(inst, run.migrationRunId)
        } else {
            mappings.findProjectNo(inst, researchNo) ?: return ApplyOutcome.waiting("과제 $inst/$researchNo 매핑 대기")
        }
        val title = row.text("title")!!
        val updatedAt = mappings.toTimestamp(row["updated_at"])
        val existing = submissions.findByKey(key)
        if (existing == null) {
            val subNo = submissions.insert(
                NewSubmission(key, projectNo, applicant, title, status, mappings.toTimestamp(row["submitted_at"]), updatedAt, "CDC", run.migrationRunId),
            )
            return ApplyOutcome.applied("신청 $key 등록 → 새 신청번호 $subNo (${status.label})")
        }
        val before = existing.status
        return when (val result = existing.applyLegacyChange(LegacySubmissionChange(title, status, projectNo, updatedAt))) {
            ChangeResult.Applied -> {
                submissions.update(existing)
                ApplyOutcome.applied("신청 $key (새 신청번호 ${existing.subNo}) ${before.label} → ${status.label}")
            }
            is ChangeResult.Ignored -> ApplyOutcome.skipped(result.reason)
            is ChangeResult.Rejected -> ApplyOutcome.failed(result.reason)
        }
    }

    private fun applyReview(run: CdcRun, event: StoredCdcEvent): ApplyOutcome {
        val row = event.row
        val inst = row.text("inst_code") ?: return ApplyOutcome.failed("필수값 누락: 기관 코드")
        val reviewId = row.text("review_id") ?: return ApplyOutcome.failed("필수값 누락: 심의 ID")
        if (event.op == ChangeOperation.DELETE) {
            val deleted = submissions.deleteReview(inst, reviewId)
            return if (deleted > 0) ApplyOutcome.applied("심의 이력 $inst/$reviewId 삭제") else ApplyOutcome.skipped("새 시스템에 없는 심의 이력 $inst/$reviewId")
        }
        val regno = row.text("regno") ?: return ApplyOutcome.failed("필수값 누락: 신청번호 ($inst/$reviewId)")
        val submission = submissions.findByKey(LegacySubmissionKey(inst, regno))
            ?: return ApplyOutcome.waiting("신청 $inst/$regno 이(가) 아직 반영되지 않아 대기합니다 (심의 이력 선도착)")
        val reviewerLegacy = row.text("reviewer_id")
        val reviewer = reviewerLegacy?.let { mappings.findUserId(inst, it) ?: return ApplyOutcome.waiting("심의위원 $inst/$it 매핑 대기") }
        val resultCode = row.text("result_code") ?: return ApplyOutcome.failed("필수값 누락: 심의 결과 ($inst/$reviewId)")
        val result = mappings.mapCode("REVIEW_RESULT", inst, resultCode)?.let { ReviewResult.valueOf(it) }
            ?: return ApplyOutcome.failed("심의 결과 코드 $resultCode 에 대응하는 새 결과가 없습니다 ($inst/$reviewId)")
        submissions.upsertReview(
            ReviewUpsert(submission.subNo, inst, reviewId, reviewer, result, row.text("opinion"), mappings.toTimestamp(row["reviewed_at"]), "CDC", run.migrationRunId),
        )
        return ApplyOutcome.applied("심의 이력 $inst/$reviewId → 신청 ${submission.subNo} (${result.label}) ${event.op.label}")
    }

    private fun applyAttachment(run: CdcRun, event: StoredCdcEvent): ApplyOutcome {
        val row = event.row
        if (event.op == ChangeOperation.DELETE) {
            return ApplyOutcome.skipped("첨부파일 삭제는 새 시스템에서 보존합니다 (${row["inst_code"]}/${row["file_id"]})")
        }
        if (row.text("object_key") == null) {
            return ApplyOutcome.skipped("파일 키가 없는 첨부 이벤트 (${row["inst_code"]}/${row["file_id"]})")
        }
        val created = fileTransfers.requestFromCdc(run.id, row)
        return ApplyOutcome.applied(
            if (created) "첨부파일 이관 작업 생성 (${row["inst_code"]}/${row["file_id"]})"
            else "이미 있는 첨부파일 이관 작업 (${row["inst_code"]}/${row["file_id"]})",
        )
    }

    private fun Map<String, String?>.text(column: String): String? = this[column]?.trim()?.takeIf { it.isNotEmpty() }
}
