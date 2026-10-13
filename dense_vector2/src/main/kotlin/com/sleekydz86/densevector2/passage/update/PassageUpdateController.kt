package com.sleekydz86.densevector2.passage.update

import com.sleekydz86.densevector2.global.passage.PassageDetail
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "02-문단 수정")
@RestController
class PassageUpdateController(private val service: PassageUpdateService) {
    @PutMapping("/api/passages/{id}")
    @Operation(summary = "문단 수정", description = "제목과 본문을 바꾸고 벡터를 다시 계산해 색인합니다.")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePassageRequest
    ): PassageDetail = service.update(id, request.title, request.body)
}
