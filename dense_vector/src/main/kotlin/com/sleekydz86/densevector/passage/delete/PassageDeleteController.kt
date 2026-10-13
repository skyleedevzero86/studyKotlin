package com.sleekydz86.densevector.passage.delete

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "03-문단 삭제")
@RestController
class PassageDeleteController(private val service: PassageDeleteService) {
    @DeleteMapping("/api/passages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "문단 삭제", description = "Elasticsearch 문서와 MySQL 행을 삭제합니다.")
    fun delete(@PathVariable id: Long) {
        service.delete(id)
    }
}
