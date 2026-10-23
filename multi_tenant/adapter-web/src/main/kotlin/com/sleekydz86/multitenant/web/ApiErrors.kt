package com.sleekydz86.multitenant.web

import com.sleekydz86.multitenant.domain.CastingNotFound
import com.sleekydz86.multitenant.domain.MissingTenant
import com.sleekydz86.multitenant.domain.OrderNotFound
import com.sleekydz86.multitenant.domain.UnknownTenant
import org.springframework.dao.DataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(OrderNotFound::class, CastingNotFound::class)
    fun missing(error: RuntimeException): ResponseEntity<ApiError> =
        ResponseEntity.status(404).body(ApiError(error.message ?: "대상을 찾을 수 없습니다."))

    @ExceptionHandler(UnknownTenant::class, MissingTenant::class)
    fun tenant(error: RuntimeException): ResponseEntity<ApiError> =
        ResponseEntity.badRequest().body(ApiError(error.message ?: "테넌트를 확인할 수 없습니다."))

    @ExceptionHandler(DataAccessException::class)
    fun dataAccess(error: DataAccessException): ResponseEntity<ApiError> {
        val tenant = generateSequence<Throwable>(error) { it.cause }
            .filterIsInstance<UnknownTenant>()
            .firstOrNull()
        val missing = generateSequence<Throwable>(error) { it.cause }
            .filterIsInstance<MissingTenant>()
            .firstOrNull()
        if (tenant != null) {
            return ResponseEntity.badRequest().body(ApiError(tenant.message ?: "등록되지 않은 테넌트입니다."))
        }
        if (missing != null) {
            return ResponseEntity.badRequest().body(ApiError(missing.message ?: "요청에 테넌트가 없습니다."))
        }
        throw error
    }
}
