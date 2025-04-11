package com.kposti.standard.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.kposti.global.app.AppConfig
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.io.path.exists
import kotlin.io.path.fileSize

object UtClass {
    object str {
        fun isBlank(str: String?): Boolean = str == null || str.trim().isEmpty()

        fun lcfirst(str: String): String = str.first().lowercase() + str.substring(1)

        fun isNotBlank(str: String?): Boolean = !isBlank(str)
    }

    object json {
        private val om: ObjectMapper = AppConfig.objectMapper

        fun toString(obj: Any): String = om.writeValueAsString(obj)
    }

    object jwt {
        fun toString(secret: String, expireSeconds: Long, body: Map<String, Any>): String {
            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            return Jwts.builder()
                .claims(body)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()
        }

        fun isValid(secret: String, jwtStr: String): Boolean {
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            return try {
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                true
            } catch (e: Exception) {
                false
            }
        }

        fun payload(secret: String, jwtStr: String): Map<String, Any>? {
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            return try {
                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .payload as Map<String, Any>
            } catch (e: Exception) {
                null
            }
        }
    }

    object file {
        private const val ORIGINAL_FILE_NAME_SEPARATOR = "--originalFileName_"
        const val META_STR_SEPARATOR = "_metaStr--"

        private val MIME_TYPE_MAP = mapOf(
            "application/json" to "json",
            "text/plain" to "txt",
            "text/html" to "html",
            "text/css" to "css",
            "application/javascript" to "js",
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/gif" to "gif",
            "image/webp" to "webp",
            "image/svg+xml" to "svg",
            "application/pdf" to "pdf",
            "application/xml" to "xml",
            "application/zip" to "zip",
            "application/gzip" to "gz",
            "application/x-tar" to "tar",
            "application/x-7z-compressed" to "7z",
            "application/vnd.rar" to "rar",
            "audio/mpeg" to "mp3",
            "audio/mp4" to "m4a",
            "audio/x-m4a" to "m4a",
            "audio/wav" to "wav",
            "video/quicktime" to "mov",
            "video/mp4" to "mp4",
            "video/webm" to "webm",
            "video/x-msvideo" to "avi"
        )

        fun downloadByHttp(url: String, dirPath: String): String =
            downloadByHttp(url, dirPath, true)

        fun downloadByHttp(url: String, dirPath: String, uniqueFilename: Boolean): String {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()

            val tempFilePath = "$dirPath/${UUID.randomUUID()}.tmp"

            mkdir(dirPath)

            // 실제 파일 다운로드
            val response = client.send(
                request,
                HttpResponse.BodyHandlers.ofFile(Path.of(tempFilePath))
            )

            // 파일 확장자 추출
            var extension = getExtensionFromResponse(response)

            if (extension == "tmp") {
                extension = getExtensionByTika(tempFilePath)
            }

            // 파일명 추출
            val filename = if (uniqueFilename)
                "${UUID.randomUUID()}$ORIGINAL_FILE_NAME_SEPARATOR${getFilenameWithoutExtFromUrl(url)}"
            else
                getFilenameWithoutExtFromUrl(url)

            val newFilePath = "$dirPath/$filename.$extension"

            mv(tempFilePath, newFilePath)

            return newFilePath
        }

        fun getExtensionByTika(filePath: String): String {
            val mimeType = AppConfig.tika.detect(filePath)
            return MIME_TYPE_MAP.getOrDefault(mimeType, "tmp")
        }

        fun mv(oldFilePath: String, newFilePath: String) {
            mkdir(Paths.get(newFilePath).parent.toString())

            Files.move(
                Path.of(oldFilePath),
                Path.of(newFilePath),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        fun mkdir(dirPath: String) {
            val path = Path.of(dirPath)
            if (path.exists()) return
            Files.createDirectories(path)
        }

        private fun getFilenameWithoutExtFromUrl(url: String): String {
            return try {
                val path = URI(url).path
                val filename = Path.of(path).fileName.toString()
                // 확장자 제거
                if (filename.contains("."))
                    filename.substring(0, filename.lastIndexOf('.'))
                else
                    filename
            } catch (e: URISyntaxException) {
                // URL에서 파일명을 추출할 수 없는 경우 타임스탬프 사용
                "download_${System.currentTimeMillis()}"
            }
        }

        private fun getExtensionFromResponse(response: HttpResponse<*>): String {
            return response.headers()
                .firstValue("Content-Type")
                .map { contentType -> MIME_TYPE_MAP.getOrDefault(contentType, "tmp") }
                .orElse("tmp")
        }

        fun delete(filePath: String) {
            Files.deleteIfExists(Path.of(filePath))
        }

        fun getOriginalFileName(filePath: String): String {
            val originalFileName = Path.of(filePath).fileName.toString()

            return if (originalFileName.contains(ORIGINAL_FILE_NAME_SEPARATOR))
                originalFileName.substring(originalFileName.indexOf(ORIGINAL_FILE_NAME_SEPARATOR) + ORIGINAL_FILE_NAME_SEPARATOR.length)
            else
                originalFileName
        }

        fun getFileExt(filePath: String): String {
            val filename = getOriginalFileName(filePath)

            return if (filename.contains("."))
                filename.substring(filename.lastIndexOf('.') + 1)
            else
                ""
        }

        fun getFileSize(filePath: String): Int = Path.of(filePath).fileSize().toInt()

        fun rm(filePath: String) {
            val path = Path.of(filePath)

            if (!path.exists()) return

            if (Files.isRegularFile(path)) {
                // 파일이면 바로 삭제
                Files.delete(path)
            } else {
                // 디렉터리면 내부 파일들 삭제 후 디렉터리 삭제
                Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        Files.delete(file)
                        return FileVisitResult.CONTINUE
                    }

                    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                        Files.delete(dir)
                        return FileVisitResult.CONTINUE
                    }
                })
            }
        }

        fun getFileExtTypeCodeFromFileExt(ext: String): String = when(ext) {
            "jpeg", "jpg", "gif", "png", "svg", "webp" -> "img"
            "mp4", "avi", "mov" -> "video"
            "mp3", "m4a" -> "audio"
            else -> "etc"
        }

        fun getFileExtType2CodeFromFileExt(ext: String): String = when(ext) {
            "jpeg", "jpg" -> "jpg"
            else -> ext
        }

        fun getMetadata(filePath: String): Map<String, Any> {
            val ext = getFileExt(filePath)
            val fileExtTypeCode = getFileExtTypeCodeFromFileExt(ext)

            return if (fileExtTypeCode == "img") getImgMetadata(filePath) else emptyMap()
        }

        private fun getImgMetadata(filePath: String): Map<String, Any> {
            val metadata = mutableMapOf<String, Any>()

            try {
                ImageIO.createImageInputStream(File(filePath)).use { input ->
                    val readers = ImageIO.getImageReaders(input)

                    if (!readers.hasNext()) {
                        throw IOException("지원되지 않는 이미지 형식: $filePath")
                    }

                    val reader = readers.next()
                    reader.input = input

                    val width = reader.getWidth(0)
                    val height = reader.getHeight(0)

                    metadata["width"] = width
                    metadata["height"] = height

                    reader.dispose()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return metadata
        }

        fun toFile(multipartFile: MultipartFile?, dirPath: String): String =
            toFile(multipartFile, dirPath, "")

        fun toFile(multipartFile: MultipartFile?, dirPath: String, metaStr: String): String {
            if (multipartFile == null || multipartFile.isEmpty()) return ""

            val fileName = if (str.isBlank(metaStr))
                "${UUID.randomUUID()}$ORIGINAL_FILE_NAME_SEPARATOR${multipartFile.originalFilename}"
            else
                "$metaStr$META_STR_SEPARATOR${UUID.randomUUID()}$ORIGINAL_FILE_NAME_SEPARATOR${multipartFile.originalFilename}"

            val filePath = "$dirPath/$fileName"

            mkdir(dirPath)
            multipartFile.transferTo(File(filePath))

            return filePath
        }

        fun copy(filePath: String, newFilePath: String) {
            mkdir(Paths.get(newFilePath).parent.toString())

            Files.copy(
                Path.of(filePath),
                Path.of(newFilePath),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        fun getContentType(fileExt: String): String {
            return MIME_TYPE_MAP.entries
                .firstOrNull { it.value == fileExt }
                ?.key ?: ""
        }

        fun withNewExt(fileName: String, fileExt: String): String {
            return if (fileName.contains("."))
                fileName.substring(0, fileName.lastIndexOf('.') + 1) + fileExt
            else
                "$fileName.$fileExt"
        }

        fun getFileExtTypeCodeFromFilePath(filePath: String): String {
            val ext = getFileExt(filePath)
            return getFileExtTypeCodeFromFileExt(ext)
        }

        fun getMetadataStrFromFileName(filePath: String): String {
            val fileName = Path.of(filePath).fileName.toString()
            return if (fileName.contains(META_STR_SEPARATOR))
                fileName.substring(0, fileName.indexOf(META_STR_SEPARATOR))
            else
                ""
        }
    }

    object cmd {
        fun runAsync(cmd: String) {
            Thread { run(cmd) }.start()
        }

        fun run(cmd: String) {
            try {
                val processBuilder = ProcessBuilder("bash", "-c", cmd)
                val process = processBuilder.start()
                process.waitFor(1, TimeUnit.MINUTES)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    object date {
        fun getCurrentDateFormatted(pattern: String): String {
            val simpleDateFormat = SimpleDateFormat(pattern)
            return simpleDateFormat.format(Date())
        }

        fun patternOf(date: LocalDateTime, pattern: String): String {
            return date.format(DateTimeFormatter.ofPattern(pattern))
        }
    }

    object url {
        fun encode(str: String): String {
            return try {
                URLEncoder.encode(str, "UTF-8")
            } catch (e: Exception) {
                str
            }
        }

        fun removeDomain(url: String): String {
            return url.replaceFirst("https?://[^/]+".toRegex(), "")
        }
    }
}