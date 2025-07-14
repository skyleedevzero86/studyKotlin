package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyStatus
import java.time.LocalDateTime

object SurveyDomainService {
    
    fun canPublish(survey: Survey): Boolean {
        return survey.status == SurveyStatus.DRAFT && 
               survey.questions.isNotEmpty() && 
               survey.questions.all { it.isValid() }
    }
    
    fun canClose(survey: Survey): Boolean {
        return survey.status == SurveyStatus.PUBLISHED
    }
    
    fun canDelete(survey: Survey): Boolean {
        return survey.status == SurveyStatus.DRAFT
    }
    
    fun calculateParticipationRate(survey: Survey, totalParticipants: Int): Double {
        return if (totalParticipants > 0) {
            (survey.participantCount.toDouble() / totalParticipants) * 100
        } else {
            0.0
        }
    }
    
    fun isSurveyExpired(survey: Survey, now: LocalDateTime): Boolean {
        return survey.period.endDate.isBefore(now)
    }
    
    fun getRemainingDays(survey: Survey, now: LocalDateTime): Long {
        return java.time.temporal.ChronoUnit.DAYS.between(now, survey.period.endDate)
    }
} 