package com.sleekydz86.cdcmigration.migration.infrastructure.persistence

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.Instant

@Mapper
interface CdcMapper {
    fun insertRun(row: CdcRunRow): Int
    fun assignNames(@Param("id") id: Long): Int
    fun findRun(@Param("id") id: Long): CdcRunRow?
    fun findRunByPrefix(@Param("topicPrefix") topicPrefix: String): CdcRunRow?
    fun findRuns(): List<CdcRunRow>
    fun findActiveRuns(): List<CdcRunRow>
    fun updateRun(row: CdcRunRow): Int
    fun upsertSnapshotTable(@Param("cdcRunId") cdcRunId: Long, @Param("tableName") tableName: String, @Param("expectedRows") expectedRows: Long?): Int
    fun snapshotProgress(@Param("cdcRunId") cdcRunId: Long, @Param("tableNames") tableNames: List<String>): List<SnapshotProgressRow>
    fun insertEvent(row: CdcEventInsertRow): Long?
    fun incrementDuplicate(@Param("cdcRunId") cdcRunId: Long, @Param("eventKey") eventKey: String): Int
    fun hasPendingBefore(@Param("cdcRunId") cdcRunId: Long, @Param("eventId") eventId: Long): Boolean
    fun findEventsByStatus(@Param("cdcRunId") cdcRunId: Long, @Param("status") status: String, @Param("limit") limit: Int): List<CdcEventRow>
    fun findEvent(@Param("eventId") eventId: Long): CdcEventRow?
    fun updateEventStatus(@Param("eventId") eventId: Long, @Param("status") status: String, @Param("message") message: String?): Int
    fun listEvents(@Param("cdcRunId") cdcRunId: Long, @Param("status") status: String?, @Param("limit") limit: Int, @Param("offset") offset: Int): List<CdcEventListRow>
    fun countEvents(@Param("cdcRunId") cdcRunId: Long, @Param("status") status: String?): Int
    fun countEventsByStatus(@Param("cdcRunId") cdcRunId: Long): List<StatusCountRow>
    fun sumDuplicates(@Param("cdcRunId") cdcRunId: Long): Int
    fun resetWaitingAttempts(@Param("cdcRunId") cdcRunId: Long): Int
}

data class CdcRunRow(
    var cdcRunId: Long? = null,
    var migrationRunId: Long = 0,
    var topicPrefix: String = "",
    var connectorName: String = "",
    var status: String = "",
    var snapshotCompleted: Boolean = false,
    var snapshotCompletedAt: Instant? = null,
    var initialLoadAt: Instant? = null,
    var lastError: String? = null,
    var createdAt: Instant? = null,
)

data class SnapshotProgressRow(
    var tableName: String = "",
    var expectedRows: Long? = null,
    var scanCompleted: Boolean = false,
    var stagedRows: Long = 0,
)

data class CdcEventInsertRow(
    var cdcRunId: Long = 0,
    var eventKey: String = "",
    var topic: String = "",
    var kafkaPartition: Int = 0,
    var kafkaOffset: Long = 0,
    var tableName: String = "",
    var sourceKey: String? = null,
    var op: String = "",
    var snapshot: Boolean = false,
    var sourceLsn: Long? = null,
    var payload: String = "",
    var status: String = "",
)

data class CdcEventRow(
    var eventId: Long = 0,
    var cdcRunId: Long = 0,
    var tableName: String = "",
    var op: String = "",
    var payload: String = "",
    var attempts: Int = 0,
)

data class CdcEventListRow(
    var eventId: Long = 0,
    var tableName: String = "",
    var op: String = "",
    var sourceKey: String? = null,
    var status: String = "",
    var attempts: Int = 0,
    var duplicateCount: Int = 0,
    var lastError: String? = null,
    var topic: String = "",
    var kafkaPartition: Int = 0,
    var kafkaOffset: Long = 0,
    var sourceLsn: Long? = null,
    var receivedAt: Instant? = null,
    var appliedAt: Instant? = null,
    var payload: String = "",
)

data class StatusCountRow(var status: String = "", var cnt: Int = 0)
