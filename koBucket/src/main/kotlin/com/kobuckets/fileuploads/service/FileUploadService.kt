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

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }

        val filename = UUID.randomUUID().toString() + "_" + file.originalFilename
        val inputStream = file.inputStream

        println("===== 파일 업로드 정보 =====")
        println("원본 파일명: ${file.originalFilename}")
        println("저장 파일명: $filename")
        println("Content Type: ${file.contentType}")
        println("파일 크기: ${file.size} bytes")
        println("MinIO 버킷 이름: $bucket")
        println("MinIO 내 저장 경로: $bucket/$filename")
        println("접근 가능한 URL: ${properties.url}/$bucket/$filename")
        println("=================================")

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(filename)
                .stream(inputStream, file.size, -1)
                .contentType(file.contentType)
                .build()
        )

        return "${properties.url}/${bucket}/${filename}"
    }

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

            // 원본 파일명 추출
            val originalFilename = if (filename.contains("_")) {
                filename.substring(filename.indexOf("_") + 1)
            } else {
                filename
            }

            // RFC 5987 완전 준수 방식으로 한글 파일명 처리
            val encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8.toString())
                .replace("+", "%20") // 공백을 %20으로 변경
                .replace("*", "%2A") // * 문자 인코딩
                .replace("'", "%27") // ' 문자 인코딩

            // ASCII 안전 파일명 생성 (fallback용)
            val asciiSafeFilename = originalFilename.replace(Regex("[^\\x00-\\x7F]"), "_")

            // Content-Disposition 헤더를 RFC 5987 표준에 맞게 설정
            val contentDisposition = "attachment; filename=\"$asciiSafeFilename\"; filename*=UTF-8''$encodedFilename"

            headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)

            // Content-Type을 강제로 application/octet-stream으로 설정하여 다운로드 유도
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)

            // 추가 보안 및 캐시 제어 헤더
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
            headers.add("Pragma", "no-cache")
            headers.add("Expires", "0")
            headers.add("X-Content-Type-Options", "nosniff") // MIME 타입 스니핑 방지

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