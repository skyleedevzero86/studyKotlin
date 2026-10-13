package com.sleekydz86.densevector2.global.elasticsearch

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "07-벡터 인덱스")
@RestController
class VectorIndexController(private val service: VectorIndexInfoService) {
    @GetMapping("/api/vector-index")
    @Operation(summary = "dense_vector 인덱스 설정 조회")
    fun index(): IndexInfoResponse = service.describe()
}
