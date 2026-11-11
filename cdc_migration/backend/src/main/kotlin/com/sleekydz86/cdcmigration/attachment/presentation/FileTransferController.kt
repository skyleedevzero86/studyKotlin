package com.sleekydz86.cdcmigration.attachment.presentation

import com.sleekydz86.cdcmigration.attachment.application.FileTransferBatchResult
import com.sleekydz86.cdcmigration.attachment.application.FileTransferService
import com.sleekydz86.cdcmigration.attachment.application.FileTransferView
import com.sleekydz86.cdcmigration.global.csv.CsvTableReader
import com.sleekydz86.cdcmigration.global.procedure.Worker
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "첨부파일 이관")
@RestController
@RequestMapping("/api/file-transfers")
class FileTransferController(
    private val csvReader: CsvTableReader,
    private val transfers: FileTransferService,
) {

    @Operation(summary = "첨부파일 이관 CSV 업로드 (구 MinIO → 새 MinIO + BYTEA)")
    @PostMapping("/csv", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun importCsv(@RequestPart("file") file: MultipartFile): FileTransferBatchResult {
        val rows = file.inputStream.use { csvReader.read(it, "첨부파일(${file.originalFilename ?: "attachments.csv"})", FileTransferService.CSV_COLUMNS) }
        return transfers.importCsv(rows)
    }

    @Operation(summary = "첨부파일 이관 작업 목록 (UNMAPPED: 신청 매핑 실패)")
    @GetMapping
    fun list(@RequestParam(required = false) status: String?, @RequestParam(defaultValue = "200") limit: Int): List<FileTransferView> =
        transfers.list(status, limit)

    @Operation(summary = "대기·매핑 실패·실패 작업 지금 다시 처리")
    @PostMapping("/process")
    fun process(): Map<String, Int> = mapOf("processed" to WorkerContext.runAs(Worker.BATCH) { transfers.processRetryable() })
}
