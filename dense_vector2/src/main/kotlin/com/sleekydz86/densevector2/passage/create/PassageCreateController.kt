package com.sleekydz86.densevector2.passage.create

import com.sleekydz86.densevector2.global.passage.PassageDetail
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "01-문단 등록")
@RestController
class PassageCreateController(private val service: PassageCreateService) {
    @PostMapping("/api/passages")
    @Operation(summary = "문단 등록", description = "본문을 임베딩해 MySQL과 Elasticsearch에 저장합니다.")
    fun create(@Valid @RequestBody request: RegisterPassageRequest): ResponseEntity<PassageDetail> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request.title, request.body))
}
