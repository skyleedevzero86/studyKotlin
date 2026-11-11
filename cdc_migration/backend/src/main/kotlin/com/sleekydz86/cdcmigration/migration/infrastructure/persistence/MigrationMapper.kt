package com.sleekydz86.cdcmigration.migration.infrastructure.persistence

import com.sleekydz86.cdcmigration.migration.domain.StagedRow
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.Instant

@Mapper
interface MigrationMapper {
    fun insertRun(row: MigrationRunRow): Int
    fun findRun(@Param("id") id: Long): MigrationRunRow?
    fun findRuns(): List<MigrationRunRow>
    fun updateRun(row: MigrationRunRow): Int
    fun countRunning(): Int
    fun insertStagedUsers(@Param("runId") runId: Long, @Param("rows") rows: List<StagedRow>): Int
    fun insertStagedProjects(@Param("runId") runId: Long, @Param("rows") rows: List<StagedRow>): Int
    fun insertStagedSubmissions(@Param("runId") runId: Long, @Param("rows") rows: List<StagedRow>): Int
    fun insertStagedReviews(@Param("runId") runId: Long, @Param("rows") rows: List<StagedRow>): Int
    fun maxRowNo(@Param("runId") runId: Long, @Param("tableName") tableName: String): Int
    fun countStaged(@Param("runId") runId: Long): List<StagedCountRow>
    fun callValidate(call: ValidateCall)
    fun callApply(call: ApplyCall)
    fun findIssues(@Param("runId") runId: Long, @Param("tableName") tableName: String?, @Param("severity") severity: String?): List<IssueRow>
    fun summarizeIssues(@Param("runId") runId: Long): List<IssueSummaryRow>
    fun verifyTables(@Param("runId") runId: Long): List<TableVerificationRow>
    fun verifyMissing(@Param("runId") runId: Long): List<FindingRow>
    fun verifyDuplicates(): List<FindingRow>
    fun verifyWrongLinks(@Param("runId") runId: Long): List<FindingRow>
    fun truncateTarget(): Int
    fun restartSequences(): Int
}

data class MigrationRunRow(
    var id: Long? = null,
    var mode: String = "",
    var status: String = "",
    var description: String? = null,
    var createdAt: Instant? = null,
    var startedAt: Instant? = null,
    var finishedAt: Instant? = null,
    var attemptCount: Int = 0,
    var errorMessage: String? = null,
    var errorCount: Int = 0,
    var infoCount: Int = 0,
    var insertedUsers: Int = 0,
    var insertedProjects: Int = 0,
    var insertedApplications: Int = 0,
    var insertedReviews: Int = 0,
)

data class StagedCountRow(var tableName: String = "", var cnt: Int = 0)

class ValidateCall(var runId: Long) {
    var errorCount: Int? = 0
    var infoCount: Int? = 0
}

class ApplyCall(var runId: Long) {
    var users: Int? = 0
    var projects: Int? = 0
    var applications: Int? = 0
    var reviews: Int? = 0
}

data class IssueRow(
    var id: Long = 0,
    var tableName: String = "",
    var rowNo: Int = 0,
    var sourceKey: String? = null,
    var issueType: String = "",
    var severity: String = "",
    var message: String = "",
)

data class IssueSummaryRow(var issueType: String = "", var severity: String = "", var cnt: Int = 0)

data class TableVerificationRow(
    var tableName: String = "",
    var staged: Int = 0,
    var excluded: Int = 0,
    var expected: Int = 0,
    var loaded: Int = 0,
)

data class FindingRow(var tableName: String = "", var sourceKey: String = "", var reason: String = "")
