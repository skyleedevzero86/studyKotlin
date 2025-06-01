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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class FileUploadService(
    private val minioClient: MinioClient,
    private val properties: MinioProperties
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

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

    // 파일 다운로드 - 한글 파일명 인코딩 문제 해결
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

            // 한글 파일명 처리를 위한 RFC 5987 방식 사용
            val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                .replace("+", "%20") // 공백 처리

            // 브라우저 호환성을 위해 두 가지 방식으로 헤더 설정
            val contentDisposition = "attachment; filename=\"${filename.replace("\"", "\\\"")}\"; filename*=UTF-8''$encodedFilename"

            headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            headers.add(HttpHeaders.CONTENT_TYPE, stat.contentType() ?: MediaType.APPLICATION_OCTET_STREAM_VALUE)

            // 추가 헤더 설정
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
            headers.add("Pragma", "no-cache")
            headers.add("Expires", "0")

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(stat.size())
                .body(InputStreamResource(response))

        } catch (e: Exception) {
            throw RuntimeException("파일 다운로드 실패: ${e.message}")
        }
    }

    // 파일 리스트 조회 (날짜 포맷팅 개선)
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

                // ZonedDateTime을 로컬 시간으로 변환하여 포맷팅
                val formattedDate = try {
                    item.lastModified().toLocalDateTime().format(dateFormatter)
                } catch (e: Exception) {
                    item.lastModified().toString() // 포맷팅 실패 시 원본 문자열 사용
                }

                fileList.add(
                    FileInfo(
                        name = item.objectName(),
                        size = item.size(),
                        lastModified = formattedDate,
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
            // 파일 존재 여부 먼저 확인
            if (!fileExists(filename)) {
                throw RuntimeException("파일을 찾을 수 없습니다: $filename")
            }

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