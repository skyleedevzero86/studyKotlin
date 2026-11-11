package com.sleekydz86.cdcmigration.global.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun business(exception: BusinessException): ResponseEntity<ApiError> {
        if (exception.errorCode.status.is5xxServerError) {
            log.warn("[{}] {}", exception.errorCode.code, exception.message, exception.cause)
        } else {
            log.info("[{}] {}", exception.errorCode.code, exception.message)
        }
        return respond(exception.errorCode, exception.detail)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validation(exception: MethodArgumentNotValidException): ResponseEntity<ApiError> =
        respond(ErrorCode.INVALID_INPUT, exception.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" })

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun missingHeader(exception: MissingRequestHeaderException): ResponseEntity<ApiError> =
        if (exception.headerName.equals("X-Request-Id", ignoreCase = true)) {
            respond(ErrorCode.REQUEST_ID_REQUIRED, null)
        } else {
            respond(ErrorCode.INVALID_INPUT, "필수 헤더가 없습니다: ${exception.headerName}")
        }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun missingParameter(exception: MissingServletRequestParameterException): ResponseEntity<ApiError> =
        respond(ErrorCode.INVALID_INPUT, "필수 파라미터가 없습니다: ${exception.parameterName}")

    @ExceptionHandler(MissingServletRequestPartException::class)
    fun missingPart(exception: MissingServletRequestPartException): ResponseEntity<ApiError> =
        respond(ErrorCode.INVALID_INPUT, "업로드할 파일이 없습니다: ${exception.requestPartName}")

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun typeMismatch(exception: MethodArgumentTypeMismatchException): ResponseEntity<ApiError> =
        respond(ErrorCode.INVALID_INPUT, "파라미터 형식이 올바르지 않습니다: ${exception.name}")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun unreadable(exception: HttpMessageNotReadableException): ResponseEntity<ApiError> =
        respond(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다.")

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun uploadSize(exception: MaxUploadSizeExceededException): ResponseEntity<ApiError> =
        respond(ErrorCode.FILE_TOO_LARGE, "요청 전체 크기 제한을 넘었습니다.")

    @ExceptionHandler(NoResourceFoundException::class)
    fun noResource(exception: NoResourceFoundException): ResponseEntity<ApiError> =
        respond(ErrorCode.NOT_FOUND, exception.resourcePath)

    @ExceptionHandler(Exception::class)
    fun unexpected(exception: Exception): ResponseEntity<ApiError> {
        log.error("예상하지 못한 오류가 발생했습니다.", exception)
        return respond(ErrorCode.INTERNAL_ERROR, null)
    }

    private fun respond(errorCode: ErrorCode, detail: String?): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.valueOf(errorCode.status.value()))
            .body(ApiError(errorCode.code, errorCode.message, detail))
}
