package com.kobuckets.fileuploads.service

import io.minio.*
import com.kobuckets.config.MinioProperties
import com.kobuckets.dto.FileInfo
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class FileUploadService(
    private val minioClient: MinioClient,
    private val properties: MinioProperties
) {

    fun upload(file: MultipartFile): String {
        val bucket = properties.bucket

        // 버킷이 없다면 생성
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }

        val filename = UUID.randomUUID().toString() + "_" + file.originalFilename

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(filename)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType)
                .build()
        )

        return "${properties.url}/${bucket}/${filename}"
    }

    // 파일 다운로드
    fun downloadFile(filename: String): ResponseEntity<InputStreamResource> {
        val bucket = properties.bucket

        try {
            val response = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(filename)
                    .build()
            )

            val stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(filename)
                    .build()
            )

            val headers = HttpHeaders()
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            headers.add(HttpHeaders.CONTENT_TYPE, stat.contentType() ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(stat.size())
                .body(InputStreamResource(response))

        } catch (e: Exception) {
            throw RuntimeException("파일 다운로드 실패: ${e.message}")
        }
    }

    // 파일 리스트 조회
    fun listFiles(): List<FileInfo> {
        val bucket = properties.bucket
        val fileList = mutableListOf<FileInfo>()

        try {
            val results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucket)
                    .build()
            )

            for (result in results) {
                val item = result.get()
                fileList.add(
                    FileInfo(
                        name = item.objectName(),
                        size = item.size(),
                        lastModified = item.lastModified().toString(),
                        url = "${properties.url}/${bucket}/${item.objectName()}"
                    )
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("파일 리스트 조회 실패: ${e.message}")
        }

        return fileList
    }

    // 파일 삭제
    fun deleteFile(filename: String): Boolean {
        val bucket = properties.bucket

        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(filename)
                    .build()
            )
            return true
        } catch (e: Exception) {
            throw RuntimeException("파일 삭제 실패: ${e.message}")
        }
    }

    // 파일 존재 여부 확인
    fun fileExists(filename: String): Boolean {
        val bucket = properties.bucket

        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(filename)
                    .build()
            )
            return true
        } catch (e: Exception) {
            return false
        }
    }
}