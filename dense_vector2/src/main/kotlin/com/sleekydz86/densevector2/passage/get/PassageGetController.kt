package com.sleekydz86.densevector2.passage.get

import com.sleekydz86.densevector2.global.passage.PassageDetail
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Tag(name = "04-문단 단건")
@RestController
class PassageGetController(private val service: PassageGetService) {
    @GetMapping("/api/passages/{id}")
    @Operation(summary = "문단 단건 조회", description = "id로 원문과 임베딩 벡터를 조회합니다.")
    fun get(@PathVariable id: Long): PassageDetail = service.get(id)
}
