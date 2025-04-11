package com.kposti.global.handler

import com.kposti.global.app.AppConfig
import com.kposti.global.exceptions.ServiceException
import com.kposti.global.https.RespData
import com.kposti.standard.base.Empty
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.NoSuchElementException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handle(ex: NoHandlerFoundException): ResponseEntity<RespData<Empty>> =
        handleException(ex, HttpStatus.NOT_FOUND, "404-1", "해당 데이터가 존재하지 않습니다.")

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handle(ex: MaxUploadSizeExceededException): ResponseEntity<RespData<Empty>> =
        handleException(ex, HttpStatus.BAD_REQUEST, "413-1", "업로드되는 파일의 용량은 ${AppConfig.getSpringServletMultipartMaxFileSize()}(을)를 초과할 수 없습니다.")

    @ExceptionHandler(NoSuchElementException::class)
    fun handle(ex: NoSuchElementException): ResponseEntity<RespData<Empty>> =
        handleException(ex, HttpStatus.NOT_FOUND, "404-1", "해당 데이터가 존재하지 않습니다.")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<RespData<Empty>> {
        if (AppConfig.isNotProd()) ex.printStackTrace()

        val message = ex.bindingResult
            .allErrors
            .filterIsInstance<FieldError>()
            .joinToString("\n") {
                "\${it.field}-\${it.code}-\${it.defaultMessage}"
            }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(RespData("400-1", message))
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServiceException::class)
    fun handle(ex: ServiceException): ResponseEntity<RespData<Empty>> {
        if (AppConfig.isNotProd()) ex.printStackTrace()

        return ResponseEntity
            .status(ex.getRsData().statusCode)
            .body(ex.getRsData())
    }

    private fun handleException(
        ex: Exception,
        status: HttpStatus,
        code: String,
        message: String
    ): ResponseEntity<RespData<Empty>> {
        if (AppConfig.isNotProd()) ex.printStackTrace()

        return ResponseEntity.status(status).body(RespData(code, message))
    }
}