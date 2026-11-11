package com.sleekydz86.cdcmigration.migration.application

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.migration.domain.LegacyRowMapper
import com.sleekydz86.cdcmigration.migration.domain.MigrationMode
import com.sleekydz86.cdcmigration.migration.domain.StagingTable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CsvMigrationService(
    private val runs: MigrationRunRepository,
    private val staging: StagingRepository,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun createRun(description: String?, tables: Map<StagingTable, List<Map<String, String?>>>): MigrationRunView {
        val missing = StagingTable.entries.filterNot { it in tables }
        if (missing.isNotEmpty()) {
            throw BusinessException(ErrorCode.INVALID_INPUT, "CSV 4개가 모두 필요합니다. 빠진 파일: ${missing.joinToString { it.csvFile }}")
        }
        val run = runs.create(MigrationMode.CSV, description?.trim()?.ifBlank { null } ?: "CSV 이관")
        tables.forEach { (table, rows) ->
            staging.insert(run.id, rows.mapIndexed { index, row -> LegacyRowMapper.toStagedRow(table, index + 1, row) })
        }
        val staged = staging.counts(run.id)
        log.info("CSV 이관 작업 {}을(를) 만들었습니다. 스테이징 건수={}", run.id, staged.mapKeys { it.key.label })
        return MigrationRunView.of(run, staged.mapKeys { it.key.name })
    }
}
