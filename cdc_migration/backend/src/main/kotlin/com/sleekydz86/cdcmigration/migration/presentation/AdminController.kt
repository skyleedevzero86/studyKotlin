package com.sleekydz86.cdcmigration.migration.presentation

import com.sleekydz86.cdcmigration.migration.application.TargetResetService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "관리")
@RestController
@RequestMapping("/api/admin")
class AdminController(private val reset: TargetResetService) {

    @Operation(summary = "대상 데이터 초기화 (두 이관 모드를 깨끗한 상태에서 따로 실행할 때)")
    @PostMapping("/reset-target")
    fun resetTarget(): Map<String, String> {
        reset.reset()
        return mapOf("message" to "대상 데이터를 초기화했습니다. 코드 매핑과 가상 과제 기준은 유지합니다.")
    }
}
