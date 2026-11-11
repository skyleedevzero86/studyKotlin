package com.sleekydz86.cdcmigration.migration.presentation

import com.sleekydz86.cdcmigration.global.csv.CsvTableReader
import com.sleekydz86.cdcmigration.migration.application.CsvMigrationService
import com.sleekydz86.cdcmigration.migration.application.MigrationExecutionService
import com.sleekydz86.cdcmigration.migration.application.MigrationQueryService
import com.sleekydz86.cdcmigration.migration.application.MigrationRunView
import com.sleekydz86.cdcmigration.migration.domain.MigrationIssue
import com.sleekydz86.cdcmigration.migration.domain.StagingTable
import com.sleekydz86.cdcmigration.migration.domain.VerificationReport
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "CSV 이관")
@RestController
@RequestMapping("/api/migration-runs")
class MigrationController(
    private val csvReader: CsvTableReader,
    private val csvMigration: CsvMigrationService,
    private val execution: MigrationExecutionService,
    private val queries: MigrationQueryService,
) {

    @Operation(summary = "CSV 4개(사용자·과제·심의 신청·심의 이력)를 올려 이관 작업 만들기 (스테이징 적재)")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun create(
        @RequestParam(required = false) description: String?,
        @RequestPart("users") users: MultipartFile,
        @RequestPart("projects") projects: MultipartFile,
        @RequestPart("applications") applications: MultipartFile,
        @RequestPart("reviews") reviews: MultipartFile,
    ): MigrationRunView = csvMigration.createRun(
        description,
        mapOf(
            StagingTable.USER to read(users, StagingTable.USER),
            StagingTable.PROJECT to read(projects, StagingTable.PROJECT),
            StagingTable.SUBMISSION to read(applications, StagingTable.SUBMISSION),
            StagingTable.REVIEW to read(reviews, StagingTable.REVIEW),
        ),
    )

    @Operation(summary = "이관 작업 목록")
    @GetMapping
    fun list(): List<MigrationRunView> = queries.list()

    @Operation(summary = "이관 작업 상세 (스테이징 건수, 오류 유형별 건수)")
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): MigrationRunView = queries.get(id)

    @Operation(summary = "이관 실행·재처리 (sp_validate_migration → sp_apply_migration)")
    @PostMapping("/{id}/execute")
    fun execute(@PathVariable id: Long): MigrationRunView {
        execution.execute(id)
        return queries.get(id)
    }

    @Operation(summary = "검증 오류 목록")
    @GetMapping("/{id}/issues")
    fun issues(
        @PathVariable id: Long,
        @RequestParam(required = false) table: String?,
        @RequestParam(required = false) severity: String?,
    ): List<MigrationIssue> = queries.issues(id, table, severity)

    @Operation(summary = "최종 검증 (총건수, 원본 키 기준 누락·중복·잘못된 연결)")
    @GetMapping("/{id}/verification")
    fun verification(@PathVariable id: Long): VerificationReport = queries.verify(id)

    private fun read(file: MultipartFile, table: StagingTable): List<Map<String, String?>> =
        file.inputStream.use { csvReader.read(it, "${table.label}(${file.originalFilename ?: table.csvFile})", table.columns) }
}
