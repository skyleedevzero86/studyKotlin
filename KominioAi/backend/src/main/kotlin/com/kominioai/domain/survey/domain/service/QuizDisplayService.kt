package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.*
import java.time.LocalDateTime

object QuizDisplayService {

    fun generateQuizStatusMessage(survey: Survey, now: LocalDateTime = LocalDateTime.now()): String =
        when {
            survey.isPeriodWaiting(now) -> {
                val daysUntilStart = survey.getPeriodRemainingDays(now)
                "${survey.getPeriodStartDate().toLocalDate()}에 퀴즈가 진행됩니다. (D-$daysUntilStart)"
            }
            survey.isPeriodActive(now) -> {
                "현재 참여 인원은 **${survey.getParticipationCount()}** 명입니다."
            }
            survey.isPeriodCompleted(now) -> {
                "퀴즈 참여 인원은 **${survey.getParticipationCount()}** 명입니다."
            }
            else -> "퀴즈 상태를 확인할 수 없습니다."
        }

    fun determineQuizButtonState(survey: Survey, now: LocalDateTime = LocalDateTime.now()): ButtonInfo =
        when {
            survey.isPeriodWaiting(now) -> ButtonInfo(
                text = "아직 시작되지 않았습니다",
                enabled = false,
                cssClass = "btn-disabled"
            )
            survey.isPeriodActive(now) && survey.getStatus() == SurveyStatus.PUBLISHED -> ButtonInfo(
                text = "퀴즈 참여하기",
                enabled = true,
                cssClass = "btn-primary",
                action = "/quiz/${survey.id.value}/participate"
            )
            survey.isPeriodCompleted(now) -> ButtonInfo(
                text = "퀴즈가 종료되었습니다",
                enabled = false,
                cssClass = "btn-disabled"
            )
            else -> ButtonInfo(
                text = "알 수 없음",
                enabled = false,
                cssClass = "btn-disabled"
            )
        }

    fun buildQuizDisplayInfo(survey: Survey, now: LocalDateTime = LocalDateTime.now()): SurveyDisplayInfo =
        SurveyDisplayInfo(
            statusMessage = generateQuizStatusMessage(survey, now),
            buttonInfo = determineQuizButtonState(survey, now),
            themeInfo = survey.getDisplayTheme(),
            participationInfo = ParticipationStatus.COMPLETED,
            requirementInfo = survey.getRequirementLevel()
        )
}