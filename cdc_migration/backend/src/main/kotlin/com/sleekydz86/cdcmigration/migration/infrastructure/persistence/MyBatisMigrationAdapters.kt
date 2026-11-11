package com.sleekydz86.cdcmigration.migration.infrastructure.persistence

import com.sleekydz86.cdcmigration.global.procedure.CudProcedureMapper
import com.sleekydz86.cdcmigration.global.procedure.ProjectCudCall
import com.sleekydz86.cdcmigration.global.procedure.UserCudCall
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import com.sleekydz86.cdcmigration.migration.application.MappingRepository
import com.sleekydz86.cdcmigration.migration.application.MigrationIssueRepository
import com.sleekydz86.cdcmigration.migration.application.MigrationProcedures
import com.sleekydz86.cdcmigration.migration.application.MigrationRunRepository
import com.sleekydz86.cdcmigration.migration.application.StagingRepository
import com.sleekydz86.cdcmigration.migration.application.TargetDataCleaner
import com.sleekydz86.cdcmigration.migration.application.VerificationQueries
import com.sleekydz86.cdcmigration.migration.domain.IssueSummary
import com.sleekydz86.cdcmigration.migration.domain.IssueType
import com.sleekydz86.cdcmigration.migration.domain.MigrationCounts
import com.sleekydz86.cdcmigration.migration.domain.MigrationIssue
import com.sleekydz86.cdcmigration.migration.domain.MigrationMode
import com.sleekydz86.cdcmigration.migration.domain.MigrationRun
import com.sleekydz86.cdcmigration.migration.domain.MigrationRunStatus
import com.sleekydz86.cdcmigration.migration.domain.StagedRow
import com.sleekydz86.cdcmigration.migration.domain.StagingTable
import com.sleekydz86.cdcmigration.migration.domain.TableVerification
import com.sleekydz86.cdcmigration.migration.domain.ValidationOutcome
import com.sleekydz86.cdcmigration.migration.domain.VerificationFinding
import com.sleekydz86.cdcmigration.migration.domain.VerificationReport
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate

@Repository
class MyBatisMigrationRunRepository(private val mapper: MigrationMapper) : MigrationRunRepository {

    override fun create(mode: MigrationMode, description: String?): MigrationRun {
        val row = MigrationRunRow(mode = mode.name, status = MigrationRunStatus.CREATED.name, description = description)
        mapper.insertRun(row)
        return find(row.id!!)!!
    }

    override fun find(id: Long): MigrationRun? = mapper.findRun(id)?.toDomain()

    override fun findAll(): List<MigrationRun> = mapper.findRuns().map { it.toDomain() }

    override fun save(run: MigrationRun) {
        mapper.updateRun(
            MigrationRunRow(
                id = run.id, mode = run.mode.name, status = run.status.name, description = run.description,
                startedAt = run.startedAt, finishedAt = run.finishedAt, attemptCount = run.attemptCount, errorMessage = run.errorMessage,
                errorCount = run.validation.errorCount, infoCount = run.validation.infoCount,
                insertedUsers = run.counts.users, insertedProjects = run.counts.projects,
                insertedApplications = run.counts.submissions, insertedReviews = run.counts.reviews,
            ),
        )
    }

    override fun anyRunning(): Boolean = mapper.countRunning() > 0

    private fun MigrationRunRow.toDomain() = MigrationRun(
        id = id!!,
        mode = MigrationMode.valueOf(mode),
        status = MigrationRunStatus.valueOf(status),
        description = description,
        createdAt = createdAt ?: Instant.EPOCH,
        startedAt = startedAt,
        finishedAt = finishedAt,
        attemptCount = attemptCount,
        errorMessage = errorMessage,
        validation = ValidationOutcome(errorCount, infoCount),
        counts = MigrationCounts(insertedUsers, insertedProjects, insertedApplications, insertedReviews),
    )
}

@Repository
class MyBatisStagingRepository(private val mapper: MigrationMapper) : StagingRepository {

    override fun insert(runId: Long, rows: List<StagedRow>) {
        rows.groupBy { it.table }.forEach { (table, tableRows) ->
            tableRows.chunked(500).forEach { chunk ->
                when (table) {
                    StagingTable.USER -> mapper.insertStagedUsers(runId, chunk)
                    StagingTable.PROJECT -> mapper.insertStagedProjects(runId, chunk)
                    StagingTable.SUBMISSION -> mapper.insertStagedSubmissions(runId, chunk)
                    StagingTable.REVIEW -> mapper.insertStagedReviews(runId, chunk)
                }
            }
        }
    }

    override fun nextRowNo(runId: Long, table: StagingTable): Int = mapper.maxRowNo(runId, table.tableName) + 1

    override fun counts(runId: Long): Map<StagingTable, Int> =
        mapper.countStaged(runId).associate { StagingTable.ofTableName(it.tableName) to it.cnt }
}

@Repository
class MyBatisMigrationProcedures(private val mapper: MigrationMapper) : MigrationProcedures {

    override fun validate(runId: Long): ValidationOutcome {
        val call = ValidateCall(runId)
        mapper.callValidate(call)
        return ValidationOutcome(call.errorCount ?: 0, call.infoCount ?: 0)
    }

    override fun apply(runId: Long): MigrationCounts {
        val call = ApplyCall(runId)
        mapper.callApply(call)
        return MigrationCounts(call.users ?: 0, call.projects ?: 0, call.applications ?: 0, call.reviews ?: 0)
    }
}

@Repository
class MyBatisMigrationIssueRepository(private val mapper: MigrationMapper) : MigrationIssueRepository {

    override fun find(runId: Long, tableName: String?, severity: String?): List<MigrationIssue> =
        mapper.findIssues(runId, tableName, severity).map {
            MigrationIssue(
                it.id, it.tableName, tableLabel(it.tableName), it.rowNo, it.sourceKey, it.issueType,
                IssueType.labelOf(it.issueType), it.severity, it.message,
            )
        }

    override fun summarize(runId: Long): List<IssueSummary> =
        mapper.summarizeIssues(runId).map { IssueSummary(it.issueType, IssueType.labelOf(it.issueType), it.severity, it.cnt) }
}

@Repository
class MyBatisVerificationQueries(private val mapper: MigrationMapper) : VerificationQueries {

    override fun verify(runId: Long): VerificationReport = VerificationReport(
        tables = mapper.verifyTables(runId).map {
            TableVerification(it.tableName, tableLabel(it.tableName), it.staged, it.excluded, it.expected, it.loaded)
        },
        missing = mapper.verifyMissing(runId).map { it.toFinding() },
        duplicates = mapper.verifyDuplicates().map { it.toFinding() },
        wrongLinks = mapper.verifyWrongLinks(runId).map { it.toFinding() },
    )

    private fun FindingRow.toFinding() = VerificationFinding(tableLabel(tableName), sourceKey, reason)
}

@Repository
class MyBatisTargetDataCleaner(private val mapper: MigrationMapper) : TargetDataCleaner {

    override fun clean() {
        mapper.truncateTarget()
        mapper.restartSequences()
    }
}

@Repository
class MyBatisMappingRepository(
    private val mapper: MappingMapper,
    private val procedures: CudProcedureMapper,
) : MappingRepository {

    override fun ensureUser(instCode: String, legacyUserId: String, runId: Long): String = mapper.ensureUser(instCode, legacyUserId, runId)

    override fun findUserId(instCode: String, legacyUserId: String): String? = mapper.findUserId(instCode, legacyUserId)

    override fun upsertUser(userId: String, instCode: String, userName: String, email: String?, deptName: String?) {
        val worker = WorkerContext.current()
        val call = UserCudCall(if (mapper.userExists(userId)) "U" else "C", worker.name, worker.ip, userId, instCode, userName, email, deptName)
        procedures.userCud(call)
        call.verify()
    }

    override fun deactivateUser(userId: String) {
        val worker = WorkerContext.current()
        val call = UserCudCall("D", worker.name, worker.ip, userId, null, null, null, null)
        procedures.userCud(call)
        call.verify()
    }

    override fun ensureProject(instCode: String, legacyResearchNo: String, runId: Long): String =
        mapper.ensureProject(instCode, legacyResearchNo, runId)

    override fun findProjectNo(instCode: String, legacyResearchNo: String): String? = mapper.findProjectNo(instCode, legacyResearchNo)

    override fun upsertProject(projectNo: String, instCode: String, title: String, piUserId: String?, startDate: LocalDate?, endDate: LocalDate?) {
        val worker = WorkerContext.current()
        val operation = if (mapper.projectExists(projectNo)) "U" else "C"
        val call = ProjectCudCall(operation, worker.name, worker.ip, projectNo, instCode, title, piUserId, startDate, endDate, false)
        procedures.projectCud(call)
        call.verify()
    }

    override fun deactivateProject(projectNo: String) {
        val worker = WorkerContext.current()
        val call = ProjectCudCall("D", worker.name, worker.ip, projectNo, null, null, null, null, null, false)
        procedures.projectCud(call)
        call.verify()
    }

    override fun virtualRuleExists(instCode: String, legacyRegno: String): Boolean = mapper.virtualRuleExists(instCode, legacyRegno)

    override fun ensureVirtualProject(instCode: String, runId: Long): String {
        mapper.ensureVirtualMapping(instCode, runId)
        val projectNo = mapper.virtualProjectNo(instCode)
        if (!mapper.projectExists(projectNo)) {
            val worker = WorkerContext.current()
            val call = ProjectCudCall("C", worker.name, worker.ip, projectNo, instCode, "가상 과제 (구시스템 과제 미연결)", null, null, null, true)
            procedures.projectCud(call)
            call.verify()
        }
        return projectNo
    }

    override fun mapCode(group: String, instCode: String, legacyCode: String): String? = mapper.mapCode(group, instCode, legacyCode)

    override fun toTimestamp(value: String?): Instant? = value?.let { mapper.toTimestamp(it) }

    override fun toDate(value: String?): LocalDate? = value?.let { mapper.toDate(it) }
}

private fun tableLabel(tableName: String): String =
    StagingTable.entries.firstOrNull { it.tableName == tableName }?.label ?: tableName
