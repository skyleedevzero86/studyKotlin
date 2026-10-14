package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import com.sleekydz86.youtubemsa.shared.StudyException
import com.sleekydz86.youtubemsa.shared.VideoId
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

@Component
class MinioRemoteStore(properties: MediaProperties) : RemoteStore {
    private val log = LoggerFactory.getLogger(javaClass)
    private val bucket = properties.minio.bucket
    private val client = MinioClient.builder()
        .endpoint(properties.minio.endpoint)
        .credentials(properties.minio.accessKey, properties.minio.secretKey)
        .build()

    @PostConstruct
    fun ensureBucket() {
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (exists) {
            log.info("MinIO 버킷이 이미 있습니다. bucket={}", bucket)
            return
        }
        client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        log.info("MinIO 버킷을 만들었습니다. bucket={}", bucket)
    }

    override fun put(downloadId: Long, videoId: VideoId, file: Path): MarkSavedBody {
        val fileName = file.fileName.toString()
        val objectKey = "${videoId.value}/$downloadId/$fileName"
        try {
            Files.newInputStream(file).use { input ->
                client.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucket)
                        .`object`(objectKey)
                        .stream(input, Files.size(file), -1)
                        .contentType(mediaType(fileName))
                        .build(),
                )
            }
        } catch (ex: Exception) {
            throw StudyException("MinIO에 파일을 올리지 못했습니다. 영상 ID: ${videoId.value}", ex, 502)
        }
        return MarkSavedBody(bucket, objectKey, fileName, Files.size(file), "")
    }

    override fun remove(bucket: String, objectKey: String) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(objectKey).build())
        } catch (ex: Exception) {
            throw StudyException("MinIO에서 보상 삭제를 하지 못했습니다. objectKey=$objectKey", ex, 502)
        }
    }
}
