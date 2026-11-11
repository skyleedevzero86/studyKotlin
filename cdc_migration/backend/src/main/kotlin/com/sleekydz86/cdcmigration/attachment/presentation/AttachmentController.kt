package com.sleekydz86.cdcmigration.attachment.presentation

import com.sleekydz86.cdcmigration.attachment.application.AttachmentQueryService
import com.sleekydz86.cdcmigration.attachment.application.AttachmentUploadService
import com.sleekydz86.cdcmigration.attachment.application.AttachmentVerification
import com.sleekydz86.cdcmigration.attachment.application.AttachmentView
import com.sleekydz86.cdcmigration.attachment.application.UploadCommand
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.StandardCharsets

@Tag(name = "첨부파일")
@RestController
class AttachmentController(
    private val uploads: AttachmentUploadService,
    private val queries: AttachmentQueryService,
) {

    @Operation(summary = "첨부파일 업로드 (MinIO 원본 + DB BYTEA, 같은 X-Request-Id로 다시 보내면 복구)")
    @PostMapping("/api/submissions/{subNo}/attachments", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @PathVariable subNo: Long,
        @RequestHeader("X-Request-Id") requestId: String,
        @RequestPart("file") file: MultipartFile,
    ): ResponseEntity<AttachmentView> {
        val attachment = uploads.upload(
            UploadCommand(subNo, requestId, file.originalFilename ?: "file", file.contentType, file.bytes),
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(AttachmentView.of(attachment))
    }

    @Operation(summary = "신청의 첨부파일 목록 (본문은 읽지 않음)")
    @GetMapping("/api/submissions/{subNo}/attachments")
    fun list(@PathVariable subNo: Long): List<AttachmentView> = queries.list(subNo)

    @Operation(summary = "첨부파일 메타데이터 (본문은 읽지 않음)")
    @GetMapping("/api/attachments/{id}")
    fun get(@PathVariable id: Long): AttachmentView = queries.get(id)

    @Operation(summary = "첨부파일 다운로드 (MinIO에서 제공)")
    @GetMapping("/api/attachments/{id}/download")
    fun download(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val (view, bytes) = queries.download(id)
        val disposition = ContentDisposition.attachment().filename(view.fileName, StandardCharsets.UTF_8).build()
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .contentType(view.contentType?.let { MediaType.parseMediaType(it) } ?: MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(bytes.size.toLong())
            .body(bytes)
    }

    @Operation(summary = "DB BYTEA와 MinIO 원본의 크기·SHA-256 비교")
    @PostMapping("/api/attachments/{id}/verify")
    fun verify(@PathVariable id: Long): AttachmentVerification = queries.verify(id)
}
