package com.kposti.global.aspect

import com.kposti.global.https.RespData
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException

@Aspect
@Component
class ResponseAspect(private val response: HttpServletResponse) {

    @Around("""
        (
            within(@org.springframework.web.bind.annotation.RestController *) &&
            (
                @annotation(GetMapping) ||
                @annotation(PostMapping) ||
                @annotation(PutMapping) ||
                @annotation(DeleteMapping) ||
                @annotation(RequestMapping)
            )
        ) || @annotation(ResponseBody)
    """)
    fun handleResponse(joinPoint: ProceedingJoinPoint): Any {
        val result = joinPoint.proceed()
        if (result is RespData<*>) {
            response.status = result.statusCode
        }
        return result
    }
}
