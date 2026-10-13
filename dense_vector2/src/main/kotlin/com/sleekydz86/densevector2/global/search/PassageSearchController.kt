package com.sleekydz86.densevector2.global.search

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "06-벡터 검색")
@RestController
class PassageSearchController(private val service: PassageSearchService) {
    @PostMapping("/api/passages/search")
    @Operation(
        summary = "코사인 kNN 검색",
        description = "검색어를 임베딩한 뒤 HNSW kNN 점수와 MySQL 벡터로 다시 계산한 코사인을 함께 반환합니다."
    )
    fun search(@Valid @RequestBody request: SearchPassageRequest): SearchResponseBody =
        service.search(request.query, request.k)
}
