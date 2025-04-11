package com.karchitecture.global.exceptions

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.karchitecture.global.exceptions.response.ExceptionResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionAdvice {

    @ExceptionHandler(Exception::class)
    fun handleException(request: HttpServletRequest, exception: Exception): ResponseEntity<ExceptionResponse> {
        log.error(
            "예상하지 못한 예외가 발생했습니다. uri: {} {}, exception : {}",
            request.method,
            request.requestURI,
            exception.message
        )

        return ResponseEntity.internalServerError()
            .body(
                ExceptionResponse(
                    name = "INTERNAL_EXCEPTION",
                    customCode = HttpStatus.INTERNAL_SERVER_ERROR,
                    message = exception.message ?: "알 수 없는 오류가 발생했습니다."
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        request: HttpServletRequest,
        exception: MethodArgumentNotValidException
    ): ResponseEntity<ExceptionResponse> {
        log.error("잘못된 param 값이 들어왔습니다. uri: {} {}", request.method, request.requestURI)

        val errorMessage = exception.bindingResult.fieldError
            ?.defaultMessage
            ?: "잘못된 요청 값"

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse("PARAM_ERROR", HttpStatus.BAD_REQUEST, errorMessage))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleDtoValueEmpty(exception: HttpMessageNotReadableException): ResponseEntity<ExceptionResponse> {
        val message = when (val causeException = exception.cause) {
            is MissingKotlinParameterException -> "요청 필드 값으로 null 값이 오면 안됩니다. 필드명: ${causeException.parameter.name}"
            else -> "요청을 역직렬화 하는과정에서 예외가 발생했습니다."
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ExceptionResponse("FIELD_EMPTY_ERROR", HttpStatus.BAD_REQUEST, message))
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        request: HttpServletRequest,
        exception: CustomException
    ): ResponseEntity<ExceptionResponse> {
        val type = exception.getExceptionType()

        log.info(
            "잘못된 요청이 들어왔습니다. uri: {} {}, 내용: {}, SPRING_LOG: {}",
            request.method, request.requestURI, type.message, exception.message
        )

        return ResponseEntity.status(type.httpStatusCode)
            .body(ExceptionResponse(type.subject, type.httpStatusCode, type.message))

    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
