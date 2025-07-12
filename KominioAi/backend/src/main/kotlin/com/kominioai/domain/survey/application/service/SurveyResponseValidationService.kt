package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.global.exception.SurveyValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class SurveyResponseValidationService(
    private val surveyRepository: SurveyRepository
) {
    
    private val logger = LoggerFactory.getLogger(SurveyResponseValidationService::class.java)
    
    /**
     * 설문 응답 제출 전 종합 검증
     */
    fun validateSurveyResponseSubmission(
        surveyId: SurveyId,
        questionIds: List<String>,
        respondentId: String?,
        ipAddress: String?
    ): Mono<Unit> {
        return surveyRepository.findByIdWithQuestions(surveyId)
            .flatMap { survey ->
                // 설문지 상태 검증
                validateSurveyStatus(survey.status)
                
                // 설문지 설정 검증
                validateSurveySettings(survey, respondentId, ipAddress)
                
                // 질문 존재 여부 및 필수 질문 검증
                validateQuestions(survey, questionIds)
                
                // 중복 응답 검증
                validateDuplicateResponse(survey, respondentId, ipAddress)
                
                Mono.just(Unit)
            }
            .switchIfEmpty(Mono.error(SurveyValidationException("존재하지 않는 설문지입니다")))
    }
    
    /**
     * 설문지 상태 검증
     */
    private fun validateSurveyStatus(status: SurveyStatus) {
        if (status != SurveyStatus.PUBLISHED) {
            logger.warn("Attempted to submit response to non-published survey: status=$status")
            throw SurveyValidationException("게시된 설문조사만 응답할 수 있습니다")
        }
    }
    
    /**
     * 설문지 설정 검증
     */
    private fun validateSurveySettings(
        survey: com.kominioai.domain.survey.domain.model.domain.Survey,
        respondentId: String?,
        ipAddress: String?
    ) {
        val settings = survey.settings
        
        // 로그인 필요 여부 검증
        if (settings.requireLogin && respondentId.isNullOrBlank()) {
            logger.warn("Login required but no respondent ID provided for survey: ${survey.id}")
            throw SurveyValidationException("로그인이 필요한 설문조사입니다")
        }
        
        // 익명 응답 허용 여부 검증
        if (!settings.allowAnonymous && respondentId.isNullOrBlank()) {
            logger.warn("Anonymous responses not allowed but no respondent ID provided for survey: ${survey.id}")
            throw SurveyValidationException("익명 응답이 허용되지 않는 설문조사입니다")
        }
        
        // IP 주소 수집 필요 여부 검증
        if (settings.collectIpAddress && ipAddress.isNullOrBlank()) {
            logger.warn("IP address collection required but no IP provided for survey: ${survey.id}")
            throw SurveyValidationException("IP 주소 수집이 필요한 설문조사입니다")
        }
    }
    
    /**
     * 질문 존재 여부 및 필수 질문 검증
     */
    private fun validateQuestions(
        survey: com.kominioai.domain.survey.domain.model.domain.Survey,
        submittedQuestionIds: List<String>
    ) {
        val surveyQuestionIds = survey.questions.map { it.id.value }.toSet()
        val submittedQuestionIdSet = submittedQuestionIds.toSet()
        
        // 존재하지 않는 질문 ID 검증
        val invalidQuestionIds = submittedQuestionIdSet - surveyQuestionIds
        if (invalidQuestionIds.isNotEmpty()) {
            logger.warn("Invalid question IDs submitted: $invalidQuestionIds for survey: ${survey.id}")
            throw SurveyValidationException("존재하지 않는 질문이 포함되어 있습니다: $invalidQuestionIds")
        }
        
        // 필수 질문 누락 검증
        val requiredQuestionIds = survey.questions
            .filter { it.required }
            .map { it.id.value }
            .toSet()
        
        val missingRequiredQuestions = requiredQuestionIds - submittedQuestionIdSet
        if (missingRequiredQuestions.isNotEmpty()) {
            logger.warn("Missing required questions: $missingRequiredQuestions for survey: ${survey.id}")
            throw SurveyValidationException("필수 질문에 대한 답변이 누락되었습니다: $missingRequiredQuestions")
        }
    }
    
    /**
     * 중복 응답 검증
     */
    private fun validateDuplicateResponse(
        survey: com.kominioai.domain.survey.domain.model.domain.Survey,
        respondentId: String?,
        ipAddress: String?
    ) {
        val settings = survey.settings
        
        // 중복 응답 허용 여부 검증
        if (!settings.allowMultipleResponses) {
            // TODO: 실제 구현에서는 데이터베이스에서 중복 응답 확인
            // 현재는 로그만 남김
            logger.info("Checking for duplicate response - survey: ${survey.id}, respondent: $respondentId, IP: $ipAddress")
        }
    }
    
    /**
     * 답변 내용의 비즈니스 로직 검증
     */
    fun validateAnswerContent(
        questionId: QuestionId,
        answerText: String?,
        selectedOptionIds: List<String>,
        questionType: com.kominioai.domain.survey.domain.valueobject.QuestionType
    ) {
        when (questionType) {
            com.kominioai.domain.survey.domain.valueobject.QuestionType.TEXT,
            com.kominioai.domain.survey.domain.valueobject.QuestionType.TEXTAREA,
            com.kominioai.domain.survey.domain.valueobject.QuestionType.NUMBER,
            com.kominioai.domain.survey.domain.valueobject.QuestionType.DATE,
            com.kominioai.domain.survey.domain.valueobject.QuestionType.EMAIL -> {
                if (answerText.isNullOrBlank()) {
                    throw SurveyValidationException("텍스트 입력이 필요한 질문입니다")
                }
                if (selectedOptionIds.isNotEmpty()) {
                    throw SurveyValidationException("텍스트 입력 질문에는 옵션을 선택할 수 없습니다")
                }
            }
            
            com.kominioai.domain.survey.domain.valueobject.QuestionType.SINGLE_CHOICE -> {
                if (selectedOptionIds.size != 1) {
                    throw SurveyValidationException("단일 선택 질문에는 정확히 1개의 옵션을 선택해야 합니다")
                }
                if (!answerText.isNullOrBlank()) {
                    throw SurveyValidationException("단일 선택 질문에는 텍스트 입력이 필요하지 않습니다")
                }
            }
            
            com.kominioai.domain.survey.domain.valueobject.QuestionType.MULTIPLE_CHOICE -> {
                if (selectedOptionIds.isEmpty()) {
                    throw SurveyValidationException("다중 선택 질문에는 최소 1개 이상의 옵션을 선택해야 합니다")
                }
                if (!answerText.isNullOrBlank()) {
                    throw SurveyValidationException("다중 선택 질문에는 텍스트 입력이 필요하지 않습니다")
                }
            }
            
            com.kominioai.domain.survey.domain.valueobject.QuestionType.RATING -> {
                if (selectedOptionIds.size != 1) {
                    throw SurveyValidationException("평점 질문에는 정확히 1개의 옵션을 선택해야 합니다")
                }
                if (!answerText.isNullOrBlank()) {
                    throw SurveyValidationException("평점 질문에는 텍스트 입력이 필요하지 않습니다")
                }
            }
            
            else -> {
                // 기본 검증
                if (answerText.isNullOrBlank() && selectedOptionIds.isEmpty()) {
                    throw SurveyValidationException("답변을 입력해주세요")
                }
            }
        }
    }
} 