package com.sleekydz86.densevector2.passage.list

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "05-문단 목록")
@RestController
class PassageListController(private val service: PassageListService) {
    @GetMapping("/api/passages")
    @Operation(summary = "문단 목록", description = "페이지 단위로 문단 목록을 조회합니다. 임베딩 배열은 포함하지 않습니다.")
    fun list(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): PassagePageResponse = service.list(page, size)
}
