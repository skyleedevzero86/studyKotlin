package com.sleekydz86.cdcmigration.migration.application.cdc

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.migration.application.CdcEventRepository
import com.sleekydz86.cdcmigration.migration.application.CdcEventPage
import com.sleekydz86.cdcmigration.migration.application.CdcRunRepository
import com.sleekydz86.cdcmigration.migration.application.ConnectorClient
import com.sleekydz86.cdcmigration.migration.application.MigrationRunRepository
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRun
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CdcQueryService(
    private val cdcRuns: CdcRunRepository,
    private val events: CdcEventRepository,
    private val runs: MigrationRunRepository,
    private val coordinator: CdcCoordinator,
    private val connectors: ConnectorClient,
) {

    fun list(): List<CdcRunView> = cdcRuns.findAll().map { view(it, includeConnector = false) }

    fun get(cdcRunId: Long): CdcRunView {
        val run = cdcRuns.find(cdcRunId) ?: throw BusinessException(ErrorCode.CDC_RUN_NOT_FOUND, cdcRunId.toString())
        return view(run, includeConnector = run.status.active)
    }

    fun events(cdcRunId: Long, status: String?, page: Int, size: Int): CdcEventPage {
        get(cdcRunId)
        val filter = status?.ifBlank { null }
        val pageSize = size.coerceIn(1, 100)
        val total = events.count(cdcRunId, filter)
        val totalPages = maxOf(1, (total + pageSize - 1) / pageSize)
        val current = page.coerceIn(1, totalPages)
        val items = events.list(cdcRunId, filter, pageSize, (current - 1) * pageSize)
        return CdcEventPage(items, current, pageSize, total, totalPages)
    }

    private fun view(run: CdcRun, includeConnector: Boolean): CdcRunView {
        val progress = cdcRuns.snapshotProgress(run.id)
        val gate = coordinator.gate(run)
        val migration = runs.find(run.migrationRunId)
        return CdcRunView(
            id = run.id,
            migrationRunId = run.migrationRunId,
            migrationStatus = migration?.status?.label,
            topicPrefix = run.topicPrefix,
            connectorName = run.connectorName,
            connectorState = if (includeConnector) runCatching { connectors.state(run.connectorName) }.getOrNull() else null,
            status = run.status.name,
            statusLabel = run.status.label,
            snapshotCompleted = run.snapshotCompleted,
            snapshotCompletedAt = run.snapshotCompletedAt,
            initialLoadAt = run.initialLoadAt,
            lastError = run.lastError,
            createdAt = run.createdAt,
            snapshotTables = progress.map { SnapshotTableView(it.table.tableName, it.table.label, it.expectedRows, it.scanCompleted, it.stagedRows) },
            initialLoadBlockers = if (run.status.name == "SNAPSHOTTING") gate.blockers() else emptyList(),
            eventCounts = events.countByStatus(run.id),
            duplicateEvents = events.duplicateTotal(run.id),
        )
    }
}

data class SnapshotTableView(val tableName: String, val label: String, val expectedRows: Long?, val scanCompleted: Boolean, val stagedRows: Long)

data class CdcRunView(
    val id: Long,
    val migrationRunId: Long,
    val migrationStatus: String?,
    val topicPrefix: String,
    val connectorName: String,
    val connectorState: String?,
    val status: String,
    val statusLabel: String,
    val snapshotCompleted: Boolean,
    val snapshotCompletedAt: Instant?,
    val initialLoadAt: Instant?,
    val lastError: String?,
    val createdAt: Instant,
    val snapshotTables: List<SnapshotTableView>,
    val initialLoadBlockers: List<String>,
    val eventCounts: Map<String, Int>,
    val duplicateEvents: Int,
)
