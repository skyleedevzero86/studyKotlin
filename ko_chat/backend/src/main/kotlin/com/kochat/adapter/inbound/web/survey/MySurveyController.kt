package com.kochat.adapter.inbound.web.survey

import com.kochat.adapter.inbound.web.survey.dto.SubmitSurveyResponseRequest
import com.kochat.adapter.inbound.web.survey.dto.SurveyDetailDto
import com.kochat.adapter.outbound.persistence.survey.SurveyParticipantJpaRepository
import com.kochat.domain.survey.model.ParticipantStatus
import com.kochat.domain.survey.service.SurveyService
import com.kochat.global.application.chat.ChatUserResolver
import com.kochat.global.config.OpenApiConfig
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "내 설문", description = "사용자에게 배정된 설문 조회·응답 API")
@RestController
@RequestMapping("/api/v1/surveys/my")
class MySurveyController(
    private val surveyParticipantJpaRepository: SurveyParticipantJpaRepository,
    private val surveyService: SurveyService,
    private val chatUserResolver: ChatUserResolver,
) {
    data class MySurveyItem(
        val surveyId: Long,
        val title: String,
        val description: String?,
        val status: String,
        val chatRoomId: Long?,
        val chatRoomName: String?,
        val hasResponded: Boolean,
    )

    @Operation(summary = "나에게 배정된 활성 설문 목록")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping
    fun mySurveys(authentication: Authentication): ResponseEntity<List<MySurveyItem>> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        val participants = surveyParticipantJpaRepository.findActiveByUserId(userId)
        val items = participants.map { p ->
            val survey = p.survey!!
            MySurveyItem(
                surveyId = survey.id!!,
                title = survey.title,
                description = survey.description,
                status = survey.status.name,
                chatRoomId = survey.chatRoom?.id,
                chatRoomName = survey.chatRoom?.name,
                hasResponded = p.status == ParticipantStatus.COMPLETED,
            )
        }
        return ResponseEntity.ok(items)
    }

    @Operation(summary = "설문 상세 조회 (참여자용)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/{surveyId}")
    fun getSurveyDetail(
        authentication: Authentication,
        @PathVariable surveyId: Long,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.getSurveyById(surveyId, userId))
    }

    @Operation(summary = "설문 응답 제출 (참여자용)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @PostMapping("/{surveyId}/responses")
    fun submitResponse(
        authentication: Authentication,
        @PathVariable surveyId: Long,
        @Valid @RequestBody request: SubmitSurveyResponseRequest,
    ): ResponseEntity<SurveyDetailDto> {
        val userId = chatUserResolver.resolveUserId(authentication.name)
        return ResponseEntity.ok(surveyService.submitResponseById(surveyId, userId, request))
    }
}
