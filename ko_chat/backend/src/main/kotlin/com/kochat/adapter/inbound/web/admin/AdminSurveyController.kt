package com.kochat.adapter.inbound.web.admin

import com.kochat.adapter.inbound.web.survey.dto.AssignRandomParticipantsRequest
import com.kochat.adapter.inbound.web.survey.dto.CreateSurveyRequest
import com.kochat.adapter.inbound.web.survey.dto.ParticipantUploadResultDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyDetailDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyRoomStatisticsListResponse
import com.kochat.adapter.inbound.web.survey.dto.SurveyStatisticsDto
import com.kochat.adapter.inbound.web.survey.dto.SurveySummaryDto
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.domain.survey.model.SurveyStatus
import com.kochat.domain.survey.model.TargetMode
import com.kochat.domain.survey.service.SurveyService
import com.kochat.domain.user.model.UserStatus
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Tag(name = "관리자 · 설문조사", description = "관리자용 설문 관리·랜덤 배정·통계 API")
@RestController
@RequestMapping("/api/v1/admin/surveys")
class AdminSurveyController(
    private val surveyService: SurveyService,
    private val chatUserResolver: ChatUserResolver,
    private val userJpaRepository: UserJpaRepository,
) {
    data class SurveyUserItem(val id: Long, val username: String, val displayName: String?)

    @Operation(summary = "설문 대상자 선택용 활성 회원 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/users")
    fun listSelectableUsers(): ResponseEntity<List<SurveyUserItem>> {
        val users = userJpaRepository.findByStatusOrderByUsernameAsc(UserStatus.ACTIVE)
            .mapNotNull { u ->
                val id = u.id ?: return@mapNotNull null
                SurveyUserItem(id, u.username!!, u.displayName)
            }
        return ResponseEntity.ok(users)
    }
    @Operation(summary = "전체 설문 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping
    fun listSurveys(
        @RequestParam(required = false) status: SurveyStatus?,
        @RequestParam(required = false) chatRoomId: Long?,
        @RequestParam(required = false) targetMode: TargetMode?,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<Page<SurveySummaryDto>> {
        val fromDateTime = from?.atStartOfDay()
        val toDateTime = to?.atTime(LocalTime.MAX)
        return ResponseEntity.ok(
            surveyService.listAdminSurveys(status, chatRoomId, targetMode, title, fromDateTime, toDateTime, pageable),
        )
    }

    @Operation(summary = "관리자 설문 생성 (채팅방 지정)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/rooms/{roomId}")
    fun createSurveyWithRoom(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @Valid @RequestBody request: CreateSurveyRequest,
    ): ResponseEntity<SurveyDetailDto> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(surveyService.adminCreateSurvey(roomId, adminUserId, request))
    }

    @Operation(summary = "관리자 설문 생성 (채팅방 없이)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping
    fun createSurvey(
        authentication: Authentication,
        @Valid @RequestBody request: CreateSurveyRequest,
    ): ResponseEntity<SurveyDetailDto> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(surveyService.adminCreateSurveyWithoutRoom(adminUserId, request))
    }

    @Operation(summary = "관리자 설문 게시")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/publish")
    fun publishSurvey(
        authentication: Authentication,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyDetailDto> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.adminPublishSurvey(surveyId, adminUserId))
    }

    @Operation(summary = "관리자 설문 종료")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/close")
    fun closeSurvey(
        authentication: Authentication,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyDetailDto> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.adminCloseSurvey(surveyId, adminUserId))
    }

    @Operation(summary = "랜덤 참여자 배정")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/assign-random")
    fun assignRandomParticipants(
        authentication: Authentication,
        @PathVariable surveyId: Long,
        @Valid @RequestBody request: AssignRandomParticipantsRequest,
    ): ResponseEntity<SurveyDetailDto> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.adminAssignRandomParticipants(surveyId, adminUserId, request))
    }

    @Operation(summary = "참여자 엑셀 업로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/participants/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadParticipants(
        authentication: Authentication,
        @PathVariable surveyId: Long,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ParticipantUploadResultDto> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.adminUploadParticipants(surveyId, adminUserId, file))
    }

    @Operation(summary = "설문 통계 (문항별·참여자별)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}/statistics")
    fun getStatistics(
        authentication: Authentication,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyStatisticsDto> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.adminGetStatistics(surveyId, adminUserId))
    }

    @Operation(summary = "설문 통계 Excel 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}/statistics/export/excel")
    fun exportStatisticsExcel(
        authentication: Authentication,
        @PathVariable surveyId: Long,
    ): ResponseEntity<ByteArray> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        val bytes = surveyService.adminExportStatistics(surveyId, adminUserId, "excel")
        return fileResponse(bytes, "survey-statistics.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    @Operation(summary = "설문 통계 PDF 다운로드")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}/statistics/export/pdf")
    fun exportStatisticsPdf(
        authentication: Authentication,
        @PathVariable surveyId: Long,
    ): ResponseEntity<ByteArray> {
        val adminUserId = chatUserResolver.resolveUserId(authentication.name)
        val bytes = surveyService.adminExportStatistics(surveyId, adminUserId, "pdf")
        return fileResponse(bytes, "survey-statistics.pdf", MediaType.APPLICATION_PDF_VALUE)
    }

    @Operation(summary = "채팅방별 설문 통계")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/statistics/by-room")
    fun roomStatistics(
        @RequestParam(required = false) surveyId: Long?,
    ): ResponseEntity<SurveyRoomStatisticsListResponse> =
        ResponseEntity.ok(surveyService.adminRoomStatistics(surveyId))

    private fun fileResponse(bytes: ByteArray, filename: String, contentType: String): ResponseEntity<ByteArray> =
        ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(bytes)
}
