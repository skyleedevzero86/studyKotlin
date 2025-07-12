package com.kominioai.global.exception.handler

import com.kominioai.global.exception.SurveyNotFoundException
import com.kominioai.global.exception.QuestionNotFoundException
import com.kominioai.global.exception.UserNotFoundException
import com.kominioai.global.exception.SurveyResponseNotFoundException
import com.kominioai.global.exception.InvalidSurveyOperationException
import com.kominioai.global.exception.SurveyValidationException
import com.kominioai.global.exception.QuestionValidationException
import com.kominioai.global.exception.QuestionOptionValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(SurveyNotFoundException::class)
    fun handleSurveyNotFound(ex: SurveyNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Survey Not Found",
            message = ex.message ?: "설문조사를 찾을 수 없습니다."
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(QuestionNotFoundException::class)
    fun handleQuestionNotFound(ex: QuestionNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Question Not Found",
            message = ex.message ?: "질문을 찾을 수 없습니다."
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "User Not Found",
            message = ex.message ?: "사용자를 찾을 수 없습니다."
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(SurveyResponseNotFoundException::class)
    fun handleSurveyResponseNotFound(ex: SurveyResponseNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Survey Response Not Found",
            message = ex.message ?: "설문 응답을 찾을 수 없습니다."
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(InvalidSurveyOperationException::class)
    fun handleInvalidSurveyOperation(ex: InvalidSurveyOperationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Survey Operation",
            message = ex.message ?: "잘못된 설문조사 작업입니다."
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(SurveyValidationException::class)
    fun handleSurveyValidation(ex: SurveyValidationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Survey Validation Error",
            message = ex.message ?: "설문조사 검증 오류가 발생했습니다."
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(QuestionValidationException::class)
    fun handleQuestionValidation(ex: QuestionValidationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Question Validation Error",
            message = ex.message ?: "질문 검증 오류가 발생했습니다."
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(QuestionOptionValidationException::class)
    fun handleQuestionOptionValidation(ex: QuestionOptionValidationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Question Option Validation Error",
            message = ex.message ?: "질문 옵션 검증 오류가 발생했습니다."
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "리소스를 찾을 수 없습니다."
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "잘못된 요청입니다."
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "상태 오류가 발생했습니다."
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }
}