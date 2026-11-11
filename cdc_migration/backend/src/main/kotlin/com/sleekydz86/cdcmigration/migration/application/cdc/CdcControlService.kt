package com.sleekydz86.cdcmigration.migration.application.cdc

import com.sleekydz86.cdcmigration.global.config.AppProperties
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.migration.application.CdcEventRepository
import com.sleekydz86.cdcmigration.migration.application.CdcRunRepository
import com.sleekydz86.cdcmigration.migration.application.ConnectorClient
import com.sleekydz86.cdcmigration.migration.application.MigrationRunRepository
import com.sleekydz86.cdcmigration.migration.domain.MigrationMode
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRun
import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Service
class CdcControlService(
    private val runs: MigrationRunRepository,
    private val cdcRuns: CdcRunRepository,
    private val events: CdcEventRepository,
    private val connectors: ConnectorClient,
    private val properties: AppProperties,
    transactionManager: PlatformTransactionManager,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val transaction = TransactionTemplate(transactionManager)

    fun start(): CdcRun {
        if (cdcRuns.findActive().isNotEmpty()) {
            throw BusinessException(ErrorCode.CDC_ALREADY_RUNNING, "진행 중인 실행을 먼저 중지하세요.")
        }
        val cdcRun = transaction.execute {
            val migrationRun = runs.create(MigrationMode.CDC, "Debezium 초기 스냅샷 적재")
            cdcRuns.create(migrationRun.id)
        }!!
        try {
            connectors.register(cdcRun.connectorName, connectorConfig(cdcRun))
        } catch (exception: BusinessException) {
            transaction.execute {
                cdcRun.fail(exception.message ?: "커넥터 등록 실패")
                cdcRuns.save(cdcRun)
            }
            throw exception
        }
        log.info("CDC 실행 {} 시작: 커넥터 {} 등록, 토픽 접두사 {}", cdcRun.id, cdcRun.connectorName, cdcRun.topicPrefix)
        return cdcRun
    }

    fun stop(cdcRunId: Long): CdcRun {
        val run = load(cdcRunId)
        connectors.delete(run.connectorName)
        transaction.execute {
            run.stop()
            cdcRuns.save(run)
        }
        log.info("CDC 실행 {} 중지: 커넥터 {} 삭제", cdcRunId, run.connectorName)
        return run
    }

    fun retryInitialLoad(cdcRunId: Long): CdcRun {
        val run = load(cdcRunId)
        transaction.execute {
            run.retryInitialLoad()
            cdcRuns.save(run)
        }
        return run
    }

    fun requeueWaiting(cdcRunId: Long): Int {
        load(cdcRunId)
        return transaction.execute { events.requeueWaiting(cdcRunId) } ?: 0
    }

    private fun load(cdcRunId: Long): CdcRun =
        cdcRuns.find(cdcRunId) ?: throw BusinessException(ErrorCode.CDC_RUN_NOT_FOUND, cdcRunId.toString())

    private fun connectorConfig(run: CdcRun): Map<String, String> {
        val legacy = properties.cdc.legacyDb
        val suffix = run.topicPrefix.substringAfter("legacy_")
        return mapOf(
            "connector.class" to "io.debezium.connector.postgresql.PostgresConnector",
            "plugin.name" to "pgoutput",
            "database.hostname" to legacy.hostname,
            "database.port" to legacy.port.toString(),
            "database.user" to legacy.user,
            "database.password" to legacy.password,
            "database.dbname" to legacy.database,
            "topic.prefix" to run.topicPrefix,
            "slot.name" to "cdc_$suffix",
            "slot.drop.on.stop" to "true",
            "publication.name" to "cdc_pub_$suffix",
            "publication.autocreate.mode" to "filtered",
            "table.include.list" to LegacyTable.entries.joinToString(",") { "public.${it.tableName}" },
            "snapshot.mode" to "initial",
            "tombstones.on.delete" to "false",
            "time.precision.mode" to "connect",
            "decimal.handling.mode" to "string",
            "key.converter" to "org.apache.kafka.connect.json.JsonConverter",
            "key.converter.schemas.enable" to "false",
            "value.converter" to "org.apache.kafka.connect.json.JsonConverter",
            "value.converter.schemas.enable" to "false",
            "notification.enabled.channels" to "sink",
            "notification.sink.topic.name" to "${run.topicPrefix}.notifications",
            "topic.creation.default.replication.factor" to "1",
            "topic.creation.default.partitions" to "1",
        )
    }
}
