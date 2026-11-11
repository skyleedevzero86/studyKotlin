package com.sleekydz86.cdcmigration.global.demo

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "실습용 장애 주입")
@RestController
@RequestMapping("/api/demo/faults")
class DemoController(private val faults: DemoFaults) {

    @Operation(summary = "장애 주입 상태")
    @GetMapping
    fun state(): DemoFaultState = faults.state()

    @Operation(summary = "다음 이관 실행 또는 다음 첨부 본문 저장을 한 번 실패시키기")
    @PutMapping
    fun arm(@RequestBody request: DemoFaultRequest): DemoFaultState {
        faults.arm(request.failNextMigration, request.failNextContentSave)
        return faults.state()
    }
}

data class DemoFaultRequest(val failNextMigration: Boolean? = null, val failNextContentSave: Boolean? = null)
