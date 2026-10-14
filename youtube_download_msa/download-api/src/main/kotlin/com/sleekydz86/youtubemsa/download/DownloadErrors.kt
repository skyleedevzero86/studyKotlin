package com.sleekydz86.youtubemsa.download

import com.sleekydz86.youtubemsa.shared.StudyException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DownloadErrors {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(StudyException::class)
    fun handle(ex: StudyException): ResponseEntity<ApiError> {
        if (ex.status >= 500) log.error("다운로드 원장 오류 status={}", ex.status, ex)
        return ResponseEntity.status(ex.status).body(ApiError(ex.message ?: "요청을 처리하지 못했습니다."))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun unreadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiError> {
        log.warn("요청 본문을 읽지 못했습니다.", ex)
        return ResponseEntity.badRequest().body(ApiError("요청 본문 JSON을 읽지 못했습니다."))
    }

    @ExceptionHandler(Exception::class)
    fun unexpected(ex: Exception): ResponseEntity<ApiError> {
        log.error("처리하지 못한 다운로드 원장 오류", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError("요청을 처리하지 못했습니다."))
    }
}
