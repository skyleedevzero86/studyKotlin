package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyStatus
import java.time.LocalDateTime

object SurveyDomainService {
    
    fun canPublish(survey: Survey): Boolean {
        return survey.canPublish()
    }
    
    fun canClose(survey: Survey): Boolean {
        return survey.canClose()
    }
    
    fun canDelete(survey: Survey): Boolean {
        return survey.getStatus() == SurveyStatus.DRAFT
    }
    
    fun canEdit(survey: Survey): Boolean {
        return survey.canEdit()
    }
    
    fun calculateParticipationRate(survey: Survey, totalParticipants: Int): Double {
        return if (totalParticipants > 0) {
            (survey.getParticipationCount().toDouble() / totalParticipants) * 100
        } else {
            0.0
        }
    }
    
    fun isSurveyExpired(survey: Survey, now: LocalDateTime = LocalDateTime.now()): Boolean {
        return survey.isPeriodCompleted(now)
    }
    
    fun getRemainingDays(survey: Survey, now: LocalDateTime = LocalDateTime.now()): Long {
        return survey.getPeriodRemainingDays(now)
    }
    
    fun getProgressPercentage(survey: Survey, now: LocalDateTime = LocalDateTime.now()): Double {
        return survey.getPeriodProgressPercentage(now)
    }
    
    fun validateSurveyForPublishing(survey: Survey): List<String> {
        val errors = mutableListOf<String>()
        
        if (!survey.canPublish()) {
            errors.add("설문을 게시할 수 없습니다.")
        }
        
        if (survey.getQuestionCount() < 1) {
            errors.add("최소 1개의 질문이 필요합니다.")
        }
        
        if (!survey.getPeriodStartDate().isAfter(LocalDateTime.now())) {
            errors.add("설문 시작일은 현재보다 이후여야 합니다.")
        }
        
        return errors
    }
    
    fun validateSurveyForClosing(survey: Survey): List<String> {
        val errors = mutableListOf<String>()
        
        if (!survey.canClose()) {
            errors.add("설문을 종료할 수 없습니다.")
        }
        
        if (!survey.isPeriodCompleted()) {
            errors.add("설문 기간이 종료되지 않았습니다.")
        }
        
        return errors
    }
} 