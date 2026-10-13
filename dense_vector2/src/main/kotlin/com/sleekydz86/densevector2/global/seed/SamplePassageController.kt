package com.sleekydz86.densevector2.global.seed

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "08-학습 문단")
@RestController
class SamplePassageController(private val loader: SamplePassageLoader) {
    @PostMapping("/api/passages/samples")
    @Operation(summary = "기본 학습 문단 적재", description = "제목이 없는 기본 학습 문단만 추가합니다.")
    fun load(): SampleLoadResponse = SampleLoadResponse(loader.load())
}
