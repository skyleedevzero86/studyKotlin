package com.sleekydz86.cdcmigration.support

import com.sleekydz86.cdcmigration.global.csv.CsvTableReader
import com.sleekydz86.cdcmigration.migration.application.CdcRunRepository
import com.sleekydz86.cdcmigration.migration.application.CsvMigrationService
import com.sleekydz86.cdcmigration.migration.application.MigrationExecutionService
import com.sleekydz86.cdcmigration.migration.application.TargetResetService
import com.sleekydz86.cdcmigration.migration.domain.MigrationRun
import com.sleekydz86.cdcmigration.migration.domain.StagingTable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import java.io.File
import java.time.Duration

@SpringBootTest
@ActiveProfiles("test")
abstract class IntegrationTestSupport {

    @Autowired
    protected lateinit var jdbc: JdbcTemplate

    @Autowired
    protected lateinit var csvReader: CsvTableReader

    @Autowired
    protected lateinit var csvMigration: CsvMigrationService

    @Autowired
    protected lateinit var execution: MigrationExecutionService

    @Autowired
    protected lateinit var reset: TargetResetService

    @Autowired
    protected lateinit var cdcRunRepository: CdcRunRepository

    protected fun resetTarget() {
        cdcRunRepository.findActive().forEach {
            it.stop()
            cdcRunRepository.save(it)
        }
        reset.reset()
    }

    protected fun count(sql: String, vararg args: Any): Int = jdbc.queryForObject(sql, Int::class.java, *args) ?: 0

    protected fun sampleCsv(name: String): File = File(SAMPLE_DIR, name)

    protected fun readCsv(table: StagingTable): List<Map<String, String?>> =
        sampleCsv(table.csvFile).inputStream().use { csvReader.read(it, table.csvFile, table.columns) }

    protected fun createSampleRun(): Long = csvMigration.createRun(
        "통합 테스트",
        StagingTable.entries.associateWith { readCsv(it) },
    ).id

    protected fun loadSampleData(): MigrationRun = execution.execute(createSampleRun())

    companion object {
        private val SAMPLE_DIR = File("../sample/csv")

        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("target_db")
            .withUsername("app")
            .withPassword("app")
            .apply { start() }

        val minio: MinIOContainer = MinIOContainer("minio/minio:RELEASE.2025-04-22T22-12-26Z").apply { start() }

        val kafka: KafkaContainer = KafkaContainer("apache/kafka:3.9.1")
            .withStartupTimeout(Duration.ofMinutes(3))
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            val separator = if (postgres.jdbcUrl.contains("?")) "&" else "?"
            registry.add("spring.datasource.url") { "${postgres.jdbcUrl}${separator}escapeSyntaxCallMode=callIfNoReturn" }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
            registry.add("app.storage.endpoint") { minio.s3URL }
            registry.add("app.storage.access-key") { minio.userName }
            registry.add("app.storage.secret-key") { minio.password }
        }
    }
}
