package com.kochat.adapter.inbound.web.admin

import com.kochat.adapter.inbound.web.admin.dto.MessageTypeYearStatisticsResponse
import com.kochat.adapter.inbound.web.admin.dto.RoomTypeDailyStatisticsResponse
import com.kochat.adapter.inbound.web.admin.dto.StatisticsPeriodResponse
import com.kochat.domain.chat.model.ChatRoomType
import com.kochat.domain.chat.model.MessageType
import com.kochat.global.application.admin.AdminStatisticsExportService
import com.kochat.global.application.admin.AdminStatisticsQueryService
import com.kochat.global.application.admin.StatisticsFilter
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "관리자 · 통계", description = "관리자용 채팅 통계 조회·엑셀·PDF보내기 API")
@RestController
@RequestMapping("/api/v1/admin/statistics")
class AdminStatisticsController(
    private val statisticsQueryService: AdminStatisticsQueryService,
    private val statisticsExportService: AdminStatisticsExportService,
) {
    @Operation(summary = "시간대별 메시지 통계")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/hourly")
    fun hourly(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) roomType: ChatRoomType?,
        @RequestParam(required = false) messageType: MessageType?,
    ): ResponseEntity<StatisticsPeriodResponse> =
        ResponseEntity.ok(
            statisticsQueryService.getHourlyStatistics(
                buildFilter(from, to, roomType, messageType),
            ),
        )

    @Operation(summary = "메시지 유형별 년도별 통계")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/message-types")
    fun messageTypes(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) roomType: ChatRoomType?,
    ): ResponseEntity<MessageTypeYearStatisticsResponse> =
        ResponseEntity.ok(
            statisticsQueryService.getMessageTypeYearStatistics(
                buildFilter(from, to, roomType, null),
            ),
        )

    @Operation(summary = "채팅방 유형별 일자별 통계")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/room-types")
    fun roomTypes(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) messageType: MessageType?,
    ): ResponseEntity<RoomTypeDailyStatisticsResponse> =
        ResponseEntity.ok(
            statisticsQueryService.getRoomTypeDailyStatistics(
                buildFilter(from, to, null, messageType),
            ),
        )

    @Operation(summary = "시간대별 통계 엑셀 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/hourly/export/excel")
    fun exportHourlyExcel(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) roomType: ChatRoomType?,
        @RequestParam(required = false) messageType: MessageType?,
    ): ResponseEntity<ByteArray> {
        val data = statisticsQueryService.getHourlyStatistics(
            buildFilter(from, to, roomType, messageType),
        )
        return fileResponse(
            statisticsExportService.exportHourlyExcel(data),
            "hourly-statistics.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        )
    }

    @Operation(summary = "시간대별 통계 PDF 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/hourly/export/pdf")
    fun exportHourlyPdf(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) roomType: ChatRoomType?,
        @RequestParam(required = false) messageType: MessageType?,
    ): ResponseEntity<ByteArray> {
        val data = statisticsQueryService.getHourlyStatistics(
            buildFilter(from, to, roomType, messageType),
        )
        return fileResponse(
            statisticsExportService.exportHourlyPdf(data),
            "hourly-statistics.pdf",
            MediaType.APPLICATION_PDF_VALUE,
        )
    }

    @Operation(summary = "메시지 유형별 통계 엑셀 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/message-types/export/excel")
    fun exportMessageTypeExcel(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) roomType: ChatRoomType?,
    ): ResponseEntity<ByteArray> {
        val data = statisticsQueryService.getMessageTypeYearStatistics(
            buildFilter(from, to, roomType, null),
        )
        return fileResponse(
            statisticsExportService.exportMessageTypeYearExcel(data),
            "message-type-statistics.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        )
    }

    @Operation(summary = "메시지 유형별 통계 PDF 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/message-types/export/pdf")
    fun exportMessageTypePdf(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) roomType: ChatRoomType?,
    ): ResponseEntity<ByteArray> {
        val data = statisticsQueryService.getMessageTypeYearStatistics(
            buildFilter(from, to, roomType, null),
        )
        return fileResponse(
            statisticsExportService.exportMessageTypeYearPdf(data),
            "message-type-statistics.pdf",
            MediaType.APPLICATION_PDF_VALUE,
        )
    }

    @Operation(summary = "채팅방 유형별 통계 엑셀 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/room-types/export/excel")
    fun exportRoomTypeExcel(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) messageType: MessageType?,
    ): ResponseEntity<ByteArray> {
        val data = statisticsQueryService.getRoomTypeDailyStatistics(
            buildFilter(from, to, null, messageType),
        )
        return fileResponse(
            statisticsExportService.exportRoomTypeDailyExcel(data),
            "room-type-statistics.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        )
    }

    @Operation(summary = "채팅방 유형별 통계 PDF 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/room-types/export/pdf")
    fun exportRoomTypePdf(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(required = false) messageType: MessageType?,
    ): ResponseEntity<ByteArray> {
        val data = statisticsQueryService.getRoomTypeDailyStatistics(
            buildFilter(from, to, null, messageType),
        )
        return fileResponse(
            statisticsExportService.exportRoomTypeDailyPdf(data),
            "room-type-statistics.pdf",
            MediaType.APPLICATION_PDF_VALUE,
        )
    }

    private fun buildFilter(
        from: LocalDate,
        to: LocalDate,
        roomType: ChatRoomType?,
        messageType: MessageType?,
    ): StatisticsFilter {
        require(!to.isBefore(from)) { "종료일은 시작일보다 빠를 수 없습니다." }
        return StatisticsFilter(from, to, roomType, messageType)
    }

    private fun fileResponse(bytes: ByteArray, filename: String, contentType: String): ResponseEntity<ByteArray> =
        ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(bytes)
}
