package com.sleekydz86.cdcmigration.migration.application

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.migration.domain.MigrationIssue
import com.sleekydz86.cdcmigration.migration.domain.VerificationReport
import org.springframework.stereotype.Service

@Service
class MigrationQueryService(
    private val runs: MigrationRunRepository,
    private val staging: StagingRepository,
    private val issues: MigrationIssueRepository,
    private val verification: VerificationQueries,
) {

    fun list(): List<MigrationRunView> = runs.findAll().map { MigrationRunView.of(it) }

    fun get(runId: Long): MigrationRunView {
        val run = runs.find(runId) ?: throw BusinessException(ErrorCode.MIGRATION_RUN_NOT_FOUND, runId.toString())
        return MigrationRunView.of(run, staging.counts(runId).mapKeys { it.key.name }, issues.summarize(runId))
    }

    fun issues(runId: Long, tableName: String?, severity: String?): List<MigrationIssue> {
        get(runId)
        return issues.find(runId, tableName?.ifBlank { null }, severity?.ifBlank { null })
    }

    fun verify(runId: Long): VerificationReport {
        get(runId)
        return verification.verify(runId)
    }
}
