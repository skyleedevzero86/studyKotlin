package com.sleekydz86.cdcmigration.migration.application

import com.sleekydz86.cdcmigration.migration.domain.IssueSummary
import com.sleekydz86.cdcmigration.migration.domain.MigrationCounts
import com.sleekydz86.cdcmigration.migration.domain.MigrationRun
import java.time.Instant

data class MigrationRunView(
    val id: Long,
    val mode: String,
    val modeLabel: String,
    val status: String,
    val statusLabel: String,
    val description: String?,
    val createdAt: Instant,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val attemptCount: Int,
    val errorMessage: String?,
    val errorCount: Int,
    val infoCount: Int,
    val inserted: MigrationCounts,
    val staged: Map<String, Int>,
    val issueSummary: List<IssueSummary>,
) {
    companion object {
        fun of(run: MigrationRun, staged: Map<String, Int> = emptyMap(), issues: List<IssueSummary> = emptyList()) = MigrationRunView(
            run.id, run.mode.name, run.mode.label, run.status.name, run.status.label, run.description, run.createdAt,
            run.startedAt, run.finishedAt, run.attemptCount, run.errorMessage, run.validation.errorCount,
            run.validation.infoCount, run.counts, staged, issues,
        )
    }
}
