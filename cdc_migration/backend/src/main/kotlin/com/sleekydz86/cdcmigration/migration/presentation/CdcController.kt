package com.sleekydz86.cdcmigration.migration.presentation

import com.sleekydz86.cdcmigration.global.procedure.Worker
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import com.sleekydz86.cdcmigration.migration.application.CdcEventPage
import com.sleekydz86.cdcmigration.migration.application.cdc.CdcControlService
import com.sleekydz86.cdcmigration.migration.application.cdc.CdcCoordinator
import com.sleekydz86.cdcmigration.migration.application.cdc.CdcQueryService
import com.sleekydz86.cdcmigration.migration.application.cdc.CdcRunView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "CDC 실습")
@RestController
@RequestMapping("/api/cdc/runs")
class CdcController(
    private val control: CdcControlService,
    private val queries: CdcQueryService,
    private val coordinator: CdcCoordinator,
) {

    @Operation(summary = "CDC 실습 시작 (Debezium 커넥터 등록, 초기 스냅샷부터)")
    @PostMapping
    fun start(): CdcRunView = queries.get(control.start().id)

    @Operation(summary = "CDC 실행 목록")
    @GetMapping
    fun list(): List<CdcRunView> = queries.list()

    @Operation(summary = "CDC 반영 현황 (스냅샷 진행, 초기 적재 대기 사유, 이벤트 상태별 건수)")
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): CdcRunView = queries.get(id)

    @Operation(summary = "CDC 이벤트 처리 기록")
    @GetMapping("/{id}/events")
    fun events(
        @PathVariable id: Long,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): CdcEventPage = queries.events(id, status, page, size)

    @Operation(summary = "CDC 중지 (커넥터 삭제, 복제 슬롯 정리)")
    @PostMapping("/{id}/stop")
    fun stop(@PathVariable id: Long): CdcRunView = queries.get(control.stop(id).id)

    @Operation(summary = "초기 적재 다시 시도")
    @PostMapping("/{id}/retry-initial-load")
    fun retryInitialLoad(@PathVariable id: Long): CdcRunView {
        control.retryInitialLoad(id)
        WorkerContext.runAs(Worker.CDC) { coordinator.process(id) }
        return queries.get(id)
    }

    @Operation(summary = "재처리 대기 이벤트 지금 다시 처리")
    @PostMapping("/{id}/retry-waiting")
    fun retryWaiting(@PathVariable id: Long): CdcRunView {
        control.requeueWaiting(id)
        WorkerContext.runAs(Worker.CDC) { coordinator.process(id) }
        return queries.get(id)
    }
}
