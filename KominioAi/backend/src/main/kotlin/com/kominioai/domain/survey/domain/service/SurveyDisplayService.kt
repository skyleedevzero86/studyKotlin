package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.*
import java.time.LocalDateTime

object SurveyDisplayService {
    fun generateStatusMessage(survey: Survey, now: LocalDateTime): String =
        when {
            survey.isWaiting(now) -> "${survey.period.startDate.toLocalDate()}에 설문이 진행됩니다. (D-${survey.getDaysUntilStart(now)})"
            survey.isActive(now) -> "현재 참여 인원은 ${survey.participantCount}명입니다."
            survey.isCompleted(now) -> "설문 참여 인원은 ${survey.participantCount}명입니다."
            else -> "설문 상태를 확인할 수 없습니다."
        }

    fun determineButtonState(survey: Survey, now: LocalDateTime): ButtonInfo =
        when {
            survey.isWaiting(now) -> ButtonInfo("아직 시작되지 않았습니다", false, "btn-disabled")
            survey.isActive(now) -> ButtonInfo("설문 참여하기", true, "btn-primary", "/surveys/${survey.id.value}/participate")
            survey.isCompleted(now) -> ButtonInfo("결과보기", true, "btn-secondary", "/surveys/${survey.id.value}/result")
            else -> ButtonInfo("알 수 없음", false, "btn-disabled")
        }

    fun calculateThemeInfo(survey: Survey): SurveyTheme =
        when (survey.surveyType) {
            SurveyType.SURVEY -> SurveyTheme("#1976d2", "#90caf9", "chart", "survey-type-survey", "fade-in")
            SurveyType.QUIZ -> SurveyTheme("#ff9800", "#ffe0b2", "question", "survey-type-quiz", "slide-in")
        }

    fun buildDisplayInfo(survey: Survey, now: LocalDateTime): SurveyDisplayInfo =
        SurveyDisplayInfo(
            statusMessage = generateStatusMessage(survey, now),
            buttonInfo = determineButtonState(survey, now),
            themeInfo = calculateThemeInfo(survey),
            participationInfo = ParticipationStatus(
                currentCount = survey.participantCount,
                targetCount = null,
                participationRate = survey.getParticipationRate(),
                lastUpdated = survey.updatedAt
            ),
            requirementInfo = survey.getRequirementLevel()
        )
}