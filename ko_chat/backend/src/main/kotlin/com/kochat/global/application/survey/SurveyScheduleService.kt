package com.kochat.global.application.survey

import com.kochat.adapter.outbound.persistence.survey.SurveyJpaRepository
import com.kochat.domain.survey.model.SurveyStatus
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SurveyScheduleService(
    private val surveyJpaRepository: SurveyJpaRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${app.survey.schedule-check-interval-ms:60000}")
    @Transactional
    fun closeExpiredSurveys() {
        val now = LocalDateTime.now()
        val expired = surveyJpaRepository.findExpiredActiveSurveys(now)
        if (expired.isEmpty()) return
        expired.forEach { survey ->
            survey.status = SurveyStatus.CLOSED
            surveyJpaRepository.save(survey)
        }
        logger.info("종료일이 지난 설문 {}건을 자동 종료했습니다.", expired.size)
    }
}
