package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.service.SurveyDisplayService
import java.time.LocalDateTime

data class QuizDetailResponse(
    val id: String,
    val title: String,
    val author: String,
    val description: String?,
    val surveyType: String,
    val status: String,
    val statusMessage: String,
    val timeLimit: Int?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val participantCount: Int,
    val participationRate: Double,
    val buttonInfo: ButtonInfoDto,
    val themeInfo: ThemeInfoDto,
    val questions: List<QuestionPreviewDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(survey: Survey, questions: List<Question>, participantCount: Int): QuizDetailResponse {
            val displayInfo = SurveyDisplayService.buildDisplayInfo(survey)

            return QuizDetailResponse(
                id = survey.id.value,
                title = survey.getTitle().value,
                author = survey.author.getDisplayName(),
                description = null,
                surveyType = survey.surveyType.name,
                status = survey.getStatus().name,
                statusMessage = displayInfo.statusMessage,
                timeLimit = survey.timeLimit?.minutes,
                startDate = survey.getPeriodStartDate(),
                endDate = survey.getPeriodEndDate(),
                participantCount = participantCount,
                participationRate = survey.getParticipationRate(),
                buttonInfo = ButtonInfoDto(
                    text = displayInfo.buttonInfo.text,
                    enabled = displayInfo.buttonInfo.enabled,
                    cssClass = displayInfo.buttonInfo.cssClass,
                    action = displayInfo.buttonInfo.action
                ),
                themeInfo = ThemeInfoDto(
                    primaryColor = displayInfo.themeInfo.primaryColor,
                    secondaryColor = displayInfo.themeInfo.secondaryColor,
                    iconType = displayInfo.themeInfo.iconType,
                    cssClassName = displayInfo.themeInfo.cssClassName,
                    animationType = displayInfo.themeInfo.animationType
                ),
                questions = questions.map { QuestionPreviewDto.from(it) },
                createdAt = survey.createdAt,
                updatedAt = survey.getUpdatedAt()
            )
        }
    }
}