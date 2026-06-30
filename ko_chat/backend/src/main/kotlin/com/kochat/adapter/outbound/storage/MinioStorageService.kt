package com.kochat.adapter.outbound.storage

import com.kochat.global.config.MinioProperties
import io.minio.BucketExistsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.http.Method
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
@ConditionalOnProperty(prefix = "app.minio", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class MinioStorageService(
    private val properties: MinioProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val client: MinioClient by lazy {
        MinioClient.builder()
            .endpoint(properties.endpoint)
            .credentials(properties.accessKey, properties.secretKey)
            .build()
    }

    @PostConstruct
    fun ensureBucket() {
        try {
            val exists = client.bucketExists(BucketExistsArgs.builder().bucket(properties.bucket).build())
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket).build())
                logger.info("MinIO 버킷 생성: {}", properties.bucket)
            }
        } catch (ex: Exception) {
            logger.warn("MinIO 버킷 초기화 실패 - 파일 업로드가 동작하지 않을 수 있습니다: {}", ex.message)
        }
    }

    fun uploadChatFile(chatRoomId: Long, file: MultipartFile): StoredObject {
        val safeName = sanitizeFileName(file.originalFilename ?: "file")
        val objectKey = "chat/$chatRoomId/${UUID.randomUUID()}-$safeName"
        val contentType = file.contentType?.takeIf { it.isNotBlank() } ?: "application/octet-stream"

        file.inputStream.use { input ->
            putObject(objectKey, input, file.size, contentType)
        }

        return StoredObject(
            objectKey = objectKey,
            fileName = safeName,
            mimeType = contentType,
            size = file.size,
            url = createPresignedUrl(objectKey),
        )
    }

    fun createPresignedUrl(objectKey: String): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(properties.bucket)
                .`object`(objectKey)
                .expiry(properties.presignExpirySeconds.toInt(), TimeUnit.SECONDS)
                .build(),
        )

    private fun putObject(objectKey: String, inputStream: InputStream, size: Long, contentType: String) {
        client.putObject(
            PutObjectArgs.builder()
                .bucket(properties.bucket)
                .`object`(objectKey)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build(),
        )
    }

    private fun sanitizeFileName(fileName: String): String =
        fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(180)

    data class StoredObject(
        val objectKey: String,
        val fileName: String,
        val mimeType: String,
        val size: Long,
        val url: String,
    )
}
