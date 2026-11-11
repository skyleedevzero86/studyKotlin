package com.sleekydz86.cdcmigration.attachment

import com.sleekydz86.cdcmigration.attachment.application.AttachmentQueryService
import com.sleekydz86.cdcmigration.attachment.application.AttachmentRepository
import com.sleekydz86.cdcmigration.attachment.application.AttachmentUploadService
import com.sleekydz86.cdcmigration.attachment.application.FileTransferService
import com.sleekydz86.cdcmigration.attachment.application.UploadCommand
import com.sleekydz86.cdcmigration.attachment.domain.AttachmentStatus
import com.sleekydz86.cdcmigration.global.demo.DemoFaults
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.support.IntegrationTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import java.io.File

@DisplayName("첨부파일 통합 테스트 (PostgreSQL BYTEA + MinIO)")
class AttachmentIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var uploads: AttachmentUploadService

    @Autowired
    private lateinit var queries: AttachmentQueryService

    @Autowired
    private lateinit var repository: AttachmentRepository

    @Autowired
    private lateinit var transfers: FileTransferService

    @Autowired
    private lateinit var faults: DemoFaults

    @Autowired
    private lateinit var s3: S3Client

    private var subNo: Long = 0

    @BeforeEach
    fun loadSubmissions() {
        resetTarget()
        loadSampleData()
        subNo = jdbc.queryForObject("SELECT sub_no FROM sub_mastinfo WHERE inst_code = 'H01' AND regnoold = '1220'", Long::class.java)!!
    }

    @Test
    @DisplayName("한 번 업로드하면 MinIO 원본과 BYTEA 본문이 같고, Kotlin ByteArray로 그대로 읽힌다")
    fun storesSameBytesInMinioAndBytea() {
        val bytes = ByteArray(4096) { (it % 251).toByte() }

        val attachment = uploads.upload(UploadCommand(subNo, "test-same-bytes", "binary.bin", "application/octet-stream", bytes))
        val verification = queries.verify(attachment.id)

        assertThat(attachment.status).isEqualTo(AttachmentStatus.READY)
        assertThat(repository.loadContent(attachment.id)).isEqualTo(bytes)
        assertThat(verification.matches).isTrue()
        assertThat(verification.databaseSize).isEqualTo(4096L)
        assertThat(verification.storageSha256).isEqualTo(attachment.fingerprint.sha256)
    }

    @Test
    @DisplayName("MinIO 업로드 뒤 DB 본문 저장이 실패하면 FAILED로 남고, 같은 요청 ID로 다시 보내면 같은 객체 키로 READY가 된다")
    fun recoversWithSameRequestId() {
        val bytes = "동의서 본문".toByteArray()
        faults.arm(migration = null, contentSave = true)

        val failure = runCatching { uploads.upload(UploadCommand(subNo, "test-retry", "동의서.txt", "text/plain", bytes)) }.exceptionOrNull()
        val failed = repository.findByRequestId("test-retry")!!
        val contentAfterFailure = repository.loadContent(failed.id)
        val recovered = uploads.upload(UploadCommand(subNo, "test-retry", "동의서.txt", "text/plain", bytes))

        assertThat((failure as BusinessException).errorCode).isEqualTo(ErrorCode.CONTENT_SAVE_FAILED)
        assertThat(failed.status).isEqualTo(AttachmentStatus.FAILED)
        assertThat(contentAfterFailure).isNull()
        assertThat(recovered.id).isEqualTo(failed.id)
        assertThat(recovered.location).isEqualTo(failed.location)
        assertThat(recovered.status).isEqualTo(AttachmentStatus.READY)
        assertThat(queries.verify(recovered.id).matches).isTrue()
    }

    @Test
    @DisplayName("같은 요청 ID로 다른 파일이 들어오면 충돌로 거부한다")
    fun rejectsDifferentFileWithSameRequestId() {
        uploads.upload(UploadCommand(subNo, "test-conflict", "a.txt", "text/plain", "첫 번째".toByteArray()))

        assertThatThrownBy { uploads.upload(UploadCommand(subNo, "test-conflict", "b.txt", "text/plain", "두 번째".toByteArray())) }
            .isInstanceOfSatisfying(BusinessException::class.java) { assertThat(it.errorCode).isEqualTo(ErrorCode.REQUEST_ID_CONFLICT) }
    }

    @Test
    @DisplayName("파일당 업로드 상한을 넘으면 거부한다")
    fun rejectsOversizedFile() {
        val bytes = ByteArray(10 * 1024 * 1024 + 1)

        assertThatThrownBy { uploads.upload(UploadCommand(subNo, "test-big", "big.bin", null, bytes)) }
            .isInstanceOfSatisfying(BusinessException::class.java) { assertThat(it.errorCode).isEqualTo(ErrorCode.FILE_TOO_LARGE) }
    }

    @Test
    @DisplayName("첨부파일 이관 CSV는 구 MinIO 파일을 새 MinIO와 BYTEA로 옮기고, 신청을 찾지 못한 파일은 매핑 실패로 남긴다")
    fun migratesLegacyFiles() {
        uploadLegacyFiles()
        val rows = sampleCsv("attachments.csv").inputStream().use { csvReader.read(it, "attachments.csv", FileTransferService.CSV_COLUMNS) }

        val first = transfers.importCsv(rows)
        val second = transfers.importCsv(rows)

        assertThat(first.total).isEqualTo(6)
        assertThat(first.done).isEqualTo(4)
        assertThat(first.unmapped).isEqualTo(2)
        assertThat(first.items.filter { it.status == "UNMAPPED" }.map { it.legacyRegno }).containsExactlyInAnyOrder("1224", "9999")
        assertThat(second.done).isEqualTo(4)
        assertThat(count("SELECT count(*) FROM attachment")).isEqualTo(4)
        val migrated = first.items.first { it.status == "DONE" }.attachmentId!!
        assertThat(queries.verify(migrated).matches).isTrue()
    }

    private fun uploadLegacyFiles() {
        val root = File("../sample/legacy-files")
        runCatching { s3.createBucket { it.bucket("legacy-attachments") } }
        root.walkTopDown().filter { it.isFile }.forEach { file ->
            val key = file.relativeTo(root).invariantSeparatorsPath
            s3.putObject({ it.bucket("legacy-attachments").key(key).contentType("text/plain") }, RequestBody.fromFile(file))
        }
    }
}
