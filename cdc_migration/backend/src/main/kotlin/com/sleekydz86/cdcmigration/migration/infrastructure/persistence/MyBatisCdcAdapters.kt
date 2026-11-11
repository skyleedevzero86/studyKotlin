package com.sleekydz86.cdcmigration.migration.infrastructure.persistence

import com.sleekydz86.cdcmigration.migration.application.CdcEventRepository
import com.sleekydz86.cdcmigration.migration.application.CdcEventView
import com.sleekydz86.cdcmigration.migration.application.CdcRunRepository
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcEventStatus
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRun
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRunStatus
import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeEvent
import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeOperation
import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import com.sleekydz86.cdcmigration.migration.domain.cdc.SnapshotTableProgress
import com.sleekydz86.cdcmigration.migration.domain.cdc.StoredCdcEvent
import org.springframework.stereotype.Repository
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID

@Repository
class MyBatisCdcRunRepository(private val mapper: CdcMapper) : CdcRunRepository {

    override fun create(migrationRunId: Long): CdcRun {
        val placeholder = "pending-${UUID.randomUUID()}"
        val row = CdcRunRow(migrationRunId = migrationRunId, topicPrefix = placeholder, connectorName = placeholder, status = CdcRunStatus.SNAPSHOTTING.name)
        mapper.insertRun(row)
        mapper.assignNames(row.cdcRunId!!)
        return find(row.cdcRunId!!)!!
    }

    override fun find(id: Long): CdcRun? = mapper.findRun(id)?.toDomain()

    override fun findByTopicPrefix(topicPrefix: String): CdcRun? = mapper.findRunByPrefix(topicPrefix)?.toDomain()

    override fun findAll(): List<CdcRun> = mapper.findRuns().map { it.toDomain() }

    override fun findActive(): List<CdcRun> = mapper.findActiveRuns().map { it.toDomain() }

    override fun save(run: CdcRun) {
        mapper.updateRun(
            CdcRunRow(
                cdcRunId = run.id, migrationRunId = run.migrationRunId, topicPrefix = run.topicPrefix, connectorName = run.connectorName,
                status = run.status.name, snapshotCompleted = run.snapshotCompleted, snapshotCompletedAt = run.snapshotCompletedAt,
                initialLoadAt = run.initialLoadAt, lastError = run.lastError,
            ),
        )
    }

    override fun recordTableScan(cdcRunId: Long, table: LegacyTable, expectedRows: Long?) {
        mapper.upsertSnapshotTable(cdcRunId, table.tableName, expectedRows)
    }

    override fun snapshotProgress(cdcRunId: Long): List<SnapshotTableProgress> =
        mapper.snapshotProgress(cdcRunId, LegacyTable.entries.map { it.tableName }).map {
            SnapshotTableProgress(LegacyTable.ofTableName(it.tableName)!!, it.expectedRows, it.scanCompleted, it.stagedRows)
        }

    private fun CdcRunRow.toDomain() = CdcRun(
        id = cdcRunId!!,
        migrationRunId = migrationRunId,
        topicPrefix = topicPrefix,
        connectorName = connectorName,
        status = CdcRunStatus.valueOf(status),
        snapshotCompleted = snapshotCompleted,
        snapshotCompletedAt = snapshotCompletedAt,
        initialLoadAt = initialLoadAt,
        lastError = lastError,
        createdAt = createdAt ?: Instant.EPOCH,
    )
}

@Repository
class MyBatisCdcEventRepository(private val mapper: CdcMapper) : CdcEventRepository {

    private val json = JsonMapper.builder().build()

    override fun insertIfAbsent(cdcRunId: Long, event: ChangeEvent, payload: String, status: CdcEventStatus): Long? {
        val eventId = mapper.insertEvent(
            CdcEventInsertRow(
                cdcRunId = cdcRunId, eventKey = event.eventKey().take(300), topic = event.position.topic,
                kafkaPartition = event.position.partition, kafkaOffset = event.position.offset, tableName = event.table.tableName,
                sourceKey = event.sourceKey().take(200), op = event.op.code, snapshot = event.snapshot, sourceLsn = event.sourceLsn,
                payload = payload, status = status.name,
            ),
        )
        if (eventId == null) {
            mapper.incrementDuplicate(cdcRunId, event.eventKey().take(300))
        }
        return eventId
    }

    override fun hasPendingBefore(cdcRunId: Long, eventId: Long): Boolean = mapper.hasPendingBefore(cdcRunId, eventId)

    override fun findByStatus(cdcRunId: Long, status: CdcEventStatus, limit: Int): List<StoredCdcEvent> =
        mapper.findEventsByStatus(cdcRunId, status.name, limit).map { it.toStored() }

    override fun find(eventId: Long): StoredCdcEvent? = mapper.findEvent(eventId)?.toStored()

    override fun updateStatus(eventId: Long, status: CdcEventStatus, message: String?) {
        mapper.updateEventStatus(eventId, status.name, message?.take(1000))
    }

    override fun list(cdcRunId: Long, status: String?, limit: Int, offset: Int): List<CdcEventView> =
        mapper.listEvents(cdcRunId, status, limit, offset).map {
            CdcEventView(
                it.eventId, it.tableName, it.op, it.sourceKey, it.status, it.attempts, it.duplicateCount, it.lastError, it.topic,
                it.kafkaPartition, it.kafkaOffset, it.sourceLsn, it.receivedAt, it.appliedAt, it.payload,
            )
        }

    override fun count(cdcRunId: Long, status: String?): Int = mapper.countEvents(cdcRunId, status)

    override fun countByStatus(cdcRunId: Long): Map<String, Int> = mapper.countEventsByStatus(cdcRunId).associate { it.status to it.cnt }

    override fun duplicateTotal(cdcRunId: Long): Int = mapper.sumDuplicates(cdcRunId)

    override fun requeueWaiting(cdcRunId: Long): Int = mapper.resetWaitingAttempts(cdcRunId)

    private fun CdcEventRow.toStored(): StoredCdcEvent {
        val root = json.readTree(payload)
        return StoredCdcEvent(
            eventId = eventId,
            cdcRunId = cdcRunId,
            table = LegacyTable.ofTableName(tableName)!!,
            op = ChangeOperation.of(op.trim())!!,
            before = rowOf(root.path("before")),
            after = rowOf(root.path("after")),
            attempts = attempts,
        )
    }

    private fun rowOf(node: JsonNode): Map<String, String?>? {
        if (node.isMissingNode || node.isNull) {
            return null
        }
        return node.properties().associate { (key, value) -> key to if (value.isNull) null else value.asString() }
    }
}
