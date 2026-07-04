package com.kochat.domain.survey.service

import com.kochat.adapter.inbound.web.survey.dto.AssignRandomParticipantsRequest
import com.kochat.adapter.inbound.web.survey.dto.CreateSurveyRequest
import com.kochat.adapter.inbound.web.survey.dto.ParticipantUploadResultDto
import com.kochat.adapter.inbound.web.survey.dto.SubmitSurveyResponseRequest
import com.kochat.adapter.inbound.web.survey.dto.SurveyDetailDto
import com.kochat.adapter.inbound.web.survey.dto.SurveyRoomStatisticsListResponse
import com.kochat.adapter.inbound.web.survey.dto.SurveyStatisticsDto
import com.kochat.adapter.inbound.web.survey.dto.SurveySummaryDto
import com.kochat.adapter.inbound.web.survey.dto.UpdateSurveyRequest
import com.kochat.domain.survey.model.SurveyStatus
import com.kochat.domain.survey.model.TargetMode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

interface SurveyService {
    fun listRoomSurveys(
        roomId: Long,
        userId: Long,
        includeAll: Boolean,
        pageable: Pageable,
    ): Page<SurveySummaryDto>

    fun getSurvey(roomId: Long, surveyId: Long, userId: Long): SurveyDetailDto

    fun createSurvey(roomId: Long, userId: Long, request: CreateSurveyRequest): SurveyDetailDto

    fun updateSurvey(roomId: Long, surveyId: Long, userId: Long, request: UpdateSurveyRequest): SurveyDetailDto

    fun publishSurvey(roomId: Long, surveyId: Long, userId: Long): SurveyDetailDto

    fun closeSurvey(roomId: Long, surveyId: Long, userId: Long): SurveyDetailDto

    fun deleteSurvey(roomId: Long, surveyId: Long, userId: Long)

    fun submitResponse(roomId: Long, surveyId: Long, userId: Long, request: SubmitSurveyResponseRequest): SurveyDetailDto

    fun getStatistics(roomId: Long, surveyId: Long, userId: Long): SurveyStatisticsDto

    fun exportStatistics(roomId: Long, surveyId: Long, userId: Long, format: String): ByteArray

    fun uploadParticipants(
        roomId: Long,
        surveyId: Long,
        userId: Long,
        file: MultipartFile,
    ): ParticipantUploadResultDto

    fun listAdminSurveys(
        status: SurveyStatus?,
        chatRoomId: Long?,
        targetMode: TargetMode?,
        title: String?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        pageable: Pageable,
    ): Page<SurveySummaryDto>

    fun adminCreateSurvey(roomId: Long, adminUserId: Long, request: CreateSurveyRequest): SurveyDetailDto

    fun adminCreateSurveyWithoutRoom(adminUserId: Long, request: CreateSurveyRequest): SurveyDetailDto

    fun adminPublishSurvey(surveyId: Long, adminUserId: Long): SurveyDetailDto

    fun adminCloseSurvey(surveyId: Long, adminUserId: Long): SurveyDetailDto

    fun adminAssignRandomParticipants(
        surveyId: Long,
        adminUserId: Long,
        request: AssignRandomParticipantsRequest,
    ): SurveyDetailDto

    fun adminUploadParticipants(
        surveyId: Long,
        adminUserId: Long,
        file: MultipartFile,
    ): ParticipantUploadResultDto

    fun adminGetStatistics(surveyId: Long, adminUserId: Long): SurveyStatisticsDto

    fun adminExportStatistics(surveyId: Long, adminUserId: Long, format: String): ByteArray

    fun adminRoomStatistics(surveyId: Long?): SurveyRoomStatisticsListResponse
}
