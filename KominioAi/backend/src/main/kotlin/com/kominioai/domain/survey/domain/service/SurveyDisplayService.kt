package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.*
import java.time.LocalDateTime

object SurveyDisplayService {

    fun generateStatusMessage(survey: Survey, now: LocalDateTime = LocalDateTime.now()): String =
        when {
            survey.isPeriodWaiting(now) -> {
                val daysUntilStart = survey.getPeriodRemainingDays(now)
                "${survey.getPeriodStartDate().toLocalDate()}에 설문이 진행됩니다. (D-$daysUntilStart)"
            }
            survey.isPeriodActive(now) -> {
                val participationRate = survey.getParticipationRate()
                "현재 참여 인원은 ${survey.getParticipationCount()}명입니다. (참여율: ${String.format("%.1f", participationRate)}%)"
            }
            survey.isPeriodCompleted(now) -> {
                val participationRate = survey.getParticipationRate()
                "설문 참여 인원은 ${survey.getParticipationCount()}명입니다. (참여율: ${String.format("%.1f", participationRate)}%)"
            }
            else -> "설문 상태를 확인할 수 없습니다."
        }

    fun determineButtonState(survey: Survey, now: LocalDateTime = LocalDateTime.now()): ButtonInfo =
        when {
            survey.isPeriodWaiting(now) -> ButtonInfo(
                text = "아직 시작되지 않았습니다",
                enabled = false,
                cssClass = "btn-disabled"
            )
            survey.isPeriodActive(now) && survey.getStatus() == SurveyStatus.PUBLISHED -> ButtonInfo(
                text = "설문 참여하기",
                enabled = true,
                cssClass = "btn-primary",
                action = "/surveys/${survey.id.value}/participate"
            )
            survey.isPeriodCompleted(now) -> ButtonInfo(
                text = "결과보기",
                enabled = true,
                cssClass = "btn-secondary",
                action = "/surveys/${survey.id.value}/result"
            )
            else -> ButtonInfo(
                text = "알 수 없음",
                enabled = false,
                cssClass = "btn-disabled"
            )
        }

    fun calculateThemeInfo(survey: Survey): SurveyTheme = survey.getDisplayTheme()

    fun buildDisplayInfo(survey: Survey, now: LocalDateTime = LocalDateTime.now()): SurveyDisplayInfo =
        SurveyDisplayInfo(
            statusMessage = generateStatusMessage(survey, now),
            buttonInfo = determineButtonState(survey, now),
            themeInfo = calculateThemeInfo(survey),
            participationInfo = ParticipationStatus.COMPLETED,
            requirementInfo = survey.getRequirementLevel()
        )
}