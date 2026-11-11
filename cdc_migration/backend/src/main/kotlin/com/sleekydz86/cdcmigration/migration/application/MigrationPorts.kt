package com.sleekydz86.cdcmigration.migration.application

import com.sleekydz86.cdcmigration.migration.domain.IssueSummary
import com.sleekydz86.cdcmigration.migration.domain.MigrationCounts
import com.sleekydz86.cdcmigration.migration.domain.MigrationIssue
import com.sleekydz86.cdcmigration.migration.domain.MigrationMode
import com.sleekydz86.cdcmigration.migration.domain.MigrationRun
import com.sleekydz86.cdcmigration.migration.domain.StagedRow
import com.sleekydz86.cdcmigration.migration.domain.StagingTable
import com.sleekydz86.cdcmigration.migration.domain.ValidationOutcome
import com.sleekydz86.cdcmigration.migration.domain.VerificationReport
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcEventStatus
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRun
import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeEvent
import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import com.sleekydz86.cdcmigration.migration.domain.cdc.SnapshotTableProgress
import com.sleekydz86.cdcmigration.migration.domain.cdc.StoredCdcEvent
import java.time.Instant
import java.time.LocalDate

interface MigrationRunRepository {
    fun create(mode: MigrationMode, description: String?): MigrationRun
    fun find(id: Long): MigrationRun?
    fun findAll(): List<MigrationRun>
    fun save(run: MigrationRun)
    fun anyRunning(): Boolean
}

interface StagingRepository {
    fun insert(runId: Long, rows: List<StagedRow>)
    fun nextRowNo(runId: Long, table: StagingTable): Int
    fun counts(runId: Long): Map<StagingTable, Int>
}

interface MigrationProcedures {
    fun validate(runId: Long): ValidationOutcome
    fun apply(runId: Long): MigrationCounts
}

interface MigrationIssueRepository {
    fun find(runId: Long, tableName: String?, severity: String?): List<MigrationIssue>
    fun summarize(runId: Long): List<IssueSummary>
}

interface VerificationQueries {
    fun verify(runId: Long): VerificationReport
}

interface TargetDataCleaner {
    fun clean()
}

interface MappingRepository {
    fun ensureUser(instCode: String, legacyUserId: String, runId: Long): String
    fun findUserId(instCode: String, legacyUserId: String): String?
    fun upsertUser(userId: String, instCode: String, userName: String, email: String?, deptName: String?)
    fun deactivateUser(userId: String)
    fun ensureProject(instCode: String, legacyResearchNo: String, runId: Long): String
    fun findProjectNo(instCode: String, legacyResearchNo: String): String?
    fun upsertProject(projectNo: String, instCode: String, title: String, piUserId: String?, startDate: LocalDate?, endDate: LocalDate?)
    fun deactivateProject(projectNo: String)
    fun virtualRuleExists(instCode: String, legacyRegno: String): Boolean
    fun ensureVirtualProject(instCode: String, runId: Long): String
    fun mapCode(group: String, instCode: String, legacyCode: String): String?
    fun toTimestamp(value: String?): Instant?
    fun toDate(value: String?): LocalDate?
}

interface CdcRunRepository {
    fun create(migrationRunId: Long): CdcRun
    fun find(id: Long): CdcRun?
    fun findByTopicPrefix(topicPrefix: String): CdcRun?
    fun findAll(): List<CdcRun>
    fun findActive(): List<CdcRun>
    fun save(run: CdcRun)
    fun recordTableScan(cdcRunId: Long, table: LegacyTable, expectedRows: Long?)
    fun snapshotProgress(cdcRunId: Long): List<SnapshotTableProgress>
}

interface CdcEventRepository {
    fun insertIfAbsent(cdcRunId: Long, event: ChangeEvent, payload: String, status: CdcEventStatus): Long?
    fun hasPendingBefore(cdcRunId: Long, eventId: Long): Boolean
    fun findByStatus(cdcRunId: Long, status: CdcEventStatus, limit: Int): List<StoredCdcEvent>
    fun find(eventId: Long): StoredCdcEvent?
    fun updateStatus(eventId: Long, status: CdcEventStatus, message: String?)
    fun list(cdcRunId: Long, status: String?, limit: Int, offset: Int): List<CdcEventView>
    fun count(cdcRunId: Long, status: String?): Int
    fun countByStatus(cdcRunId: Long): Map<String, Int>
    fun duplicateTotal(cdcRunId: Long): Int
    fun requeueWaiting(cdcRunId: Long): Int
}

data class CdcEventPage(
    val items: List<CdcEventView>,
    val page: Int,
    val size: Int,
    val total: Int,
    val totalPages: Int,
)

data class CdcEventView(
    val eventId: Long,
    val tableName: String,
    val op: String,
    val sourceKey: String?,
    val status: String,
    val attempts: Int,
    val duplicateCount: Int,
    val message: String?,
    val topic: String,
    val kafkaPartition: Int,
    val kafkaOffset: Long,
    val sourceLsn: Long?,
    val receivedAt: Instant?,
    val appliedAt: Instant?,
    val payload: String,
)

interface ConnectorClient {
    fun register(name: String, config: Map<String, String>)
    fun delete(name: String)
    fun state(name: String): String?
}

interface FileTransferRequester {
    fun requestFromCdc(cdcRunId: Long, row: Map<String, String?>): Boolean
}
