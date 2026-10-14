package com.sleekydz86.youtubedownload.global.minio

import com.sleekydz86.youtubedownload.global.env.StudyProperties
import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.RemoteFile
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.read.FileLink
import com.sleekydz86.youtubedownload.video.save.RemoteFileStore
import io.minio.BucketExistsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.http.Method
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@Component
class MinioFiles(properties: StudyProperties) : RemoteFileStore, FileLink {
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

    override fun put(downloadId: Long, videoId: VideoId, file: Path): RemoteFile {
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
            throw DownloadException(
                "MinIO에 파일을 올리지 못했습니다. 영상 ID: ${videoId.value}",
                ex,
                HttpStatus.BAD_GATEWAY,
            )
        }
        return RemoteFile(bucket, objectKey, fileName, Files.size(file))
    }

    override fun link(bucket: String, objectKey: String): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .`object`(objectKey)
                .expiry(15, TimeUnit.MINUTES)
                .build(),
        )
}
