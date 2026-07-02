package com.kochat.adapter.inbound.web.survey

import com.kochat.adapter.inbound.web.survey.dto.CreateSurveyRequest
import com.kochat.adapter.inbound.web.survey.dto.ParticipantUploadResultDto
import com.kochat.adapter.inbound.web.survey.dto.SubmitSurveyResponseRequest
import com.kochat.adapter.inbound.web.survey.dto.SurveyDetailDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyStatisticsDto
import com.kochat.adapter.inbound.web.survey.dto.SurveySummaryDto
import com.kochat.adapter.inbound.web.survey.dto.UpdateSurveyRequest
import com.kochat.domain.survey.service.SurveyService
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "설문조사", description = "채팅방 설문 생성·응답·통계 API")
@RestController
@RequestMapping("/api/v1/chat-rooms/{roomId}/surveys")
class SurveyController(
    private val surveyService: SurveyService,
    private val chatUserResolver: ChatUserResolver,
) {
    @Operation(summary = "채팅방 설문 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping
    fun listSurveys(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @RequestParam(defaultValue = "false") includeAll: Boolean,
        @PageableDefault(size = 10) pageable: Pageable,
    ): ResponseEntity<Page<SurveySummaryDto>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.listRoomSurveys(roomId, userId, includeAll, pageable))
    }

    @Operation(summary = "설문 상세")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}")
    fun getSurvey(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.getSurvey(roomId, surveyId, userId))
    }

    @Operation(summary = "설문 생성")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping
    fun createSurvey(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @Valid @RequestBody request: CreateSurveyRequest,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(surveyService.createSurvey(roomId, userId, request))
    }

    @Operation(summary = "설문 수정")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PutMapping("/{surveyId}")
    fun updateSurvey(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
        @Valid @RequestBody request: UpdateSurveyRequest,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.updateSurvey(roomId, surveyId, userId, request))
    }

    @Operation(summary = "설문 게시")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/publish")
    fun publishSurvey(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.publishSurvey(roomId, surveyId, userId))
    }

    @Operation(summary = "설문 종료")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/close")
    fun closeSurvey(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.closeSurvey(roomId, surveyId, userId))
    }

    @Operation(summary = "설문 삭제")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @DeleteMapping("/{surveyId}")
    fun deleteSurvey(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
    ): ResponseEntity<Void> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        surveyService.deleteSurvey(roomId, surveyId, userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "설문 응답 제출")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/responses")
    fun submitResponse(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
        @Valid @RequestBody request: SubmitSurveyResponseRequest,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.submitResponse(roomId, surveyId, userId, request))
    }

    @Operation(summary = "설문 통계 (방장)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}/statistics")
    fun getStatistics(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyStatisticsDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.getStatistics(roomId, surveyId, userId))
    }

    @Operation(summary = "설문 통계 Excel 다운로드 (방장)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}/statistics/export/excel")
    fun exportStatisticsExcel(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
    ): ResponseEntity<ByteArray> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        val bytes = surveyService.exportStatistics(roomId, surveyId, userId, "excel")
        return fileResponse(bytes, "survey-statistics.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    @Operation(summary = "설문 통계 PDF 다운로드 (방장)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}/statistics/export/pdf")
    fun exportStatisticsPdf(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
    ): ResponseEntity<ByteArray> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        val bytes = surveyService.exportStatistics(roomId, surveyId, userId, "pdf")
        return fileResponse(bytes, "survey-statistics.pdf", MediaType.APPLICATION_PDF_VALUE)
    }

    @Operation(summary = "참여자 엑셀 업로드 (방장)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/participants/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadParticipants(
        authentication: Authentication,
        @PathVariable roomId: Long,
        @PathVariable surveyId: Long,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<ParticipantUploadResultDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.uploadParticipants(roomId, surveyId, userId, file))
    }

    private fun fileResponse(bytes: ByteArray, filename: String, contentType: String): ResponseEntity<ByteArray> =
        ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(bytes)
}
