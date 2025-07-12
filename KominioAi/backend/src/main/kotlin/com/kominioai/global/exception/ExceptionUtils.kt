package com.kominioai.global.exception

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import org.slf4j.LoggerFactory

/**
 * 예외 처리를 표준화하기 위한 유틸리티 클래스
 * 모든 예외 메시지의 일관성을 보장합니다.
 */
object ExceptionUtils {
    
    private val logger = LoggerFactory.getLogger(ExceptionUtils::class.java)
    
    /**
     * SurveyNotFoundException을 생성하는 표준화된 메서드
     * @param surveyId 설문조사 ID (SurveyId 객체 또는 String)
     * @param operation 수행하려던 작업 (예: "질문 추가", "설문조사 조회")
     * @return SurveyNotFoundException
     */
    fun createSurveyNotFoundException(surveyId: Any, operation: String = "조회"): SurveyNotFoundException {
        val surveyIdString = when (surveyId) {
            is SurveyId -> surveyId.value
            is String -> surveyId
            else -> surveyId.toString()
        }
        
        val message = "설문조사를 찾을 수 없습니다. [ID: $surveyIdString, 작업: $operation]"
        
        logger.warn("SurveyNotFoundException 발생: $message")
        return SurveyNotFoundException(message)
    }
    
    /**
     * QuestionNotFoundException을 생성하는 표준화된 메서드
     * @param questionId 질문 ID
     * @param operation 수행하려던 작업
     * @return QuestionNotFoundException
     */
    fun createQuestionNotFoundException(questionId: Any, operation: String = "조회"): QuestionNotFoundException {
        val questionIdString = when (questionId) {
            is QuestionId -> questionId.value
            is String -> questionId
            else -> questionId.toString()
        }
        
        val message = "질문을 찾을 수 없습니다. [ID: $questionIdString, 작업: $operation]"
        
        logger.warn("QuestionNotFoundException 발생: $message")
        return QuestionNotFoundException(message)
    }
    
    /**
     * UserNotFoundException을 생성하는 표준화된 메서드
     * @param userId 사용자 ID
     * @param operation 수행하려던 작업
     * @return UserNotFoundException
     */
    fun createUserNotFoundException(userId: Any, operation: String = "조회"): UserNotFoundException {
        val userIdString = when (userId) {
            is UserId -> userId.value
            is String -> userId
            else -> userId.toString()
        }
        
        val message = "사용자를 찾을 수 없습니다. [ID: $userIdString, 작업: $operation]"
        
        logger.warn("UserNotFoundException 발생: $message")
        return UserNotFoundException(message)
    }
    
    /**
     * SurveyResponseNotFoundException을 생성하는 표준화된 메서드
     * @param responseId 응답 ID
     * @param operation 수행하려던 작업
     * @return SurveyResponseNotFoundException
     */
    fun createSurveyResponseNotFoundException(responseId: Any, operation: String = "조회"): SurveyResponseNotFoundException {
        val responseIdString = when (responseId) {
            is ResponseId -> responseId.value
            is String -> responseId
            else -> responseId.toString()
        }
        
        val message = "설문 응답을 찾을 수 없습니다. [ID: $responseIdString, 작업: $operation]"
        
        logger.warn("SurveyResponseNotFoundException 발생: $message")
        return SurveyResponseNotFoundException(message)
    }
    
    /**
     * InvalidSurveyOperationException을 생성하는 표준화된 메서드
     * @param surveyId 설문조사 ID
     * @param operation 수행하려던 작업
     * @param reason 실패 이유
     * @return InvalidSurveyOperationException
     */
    fun createInvalidSurveyOperationException(surveyId: Any, operation: String, reason: String): InvalidSurveyOperationException {
        val surveyIdString = when (surveyId) {
            is SurveyId -> surveyId.value
            is String -> surveyId
            else -> surveyId.toString()
        }
        
        val message = "설문조사 작업을 수행할 수 없습니다. [ID: $surveyIdString, 작업: $operation, 이유: $reason]"
        
        logger.warn("InvalidSurveyOperationException 발생: $message")
        return InvalidSurveyOperationException(message)
    }
    
    /**
     * SurveyValidationException을 생성하는 표준화된 메서드
     * @param field 검증 실패한 필드
     * @param value 검증 실패한 값
     * @param reason 실패 이유
     * @return SurveyValidationException
     */
    fun createSurveyValidationException(field: String, value: Any? = null, reason: String): SurveyValidationException {
        val valueString = value?.toString() ?: "null"
        val message = "설문조사 검증 실패. [필드: $field, 값: $valueString, 이유: $reason]"
        
        logger.warn("SurveyValidationException 발생: $message")
        return SurveyValidationException(message)
    }
    
    /**
     * 예외 메시지에 포함될 공통 정보를 포맷팅
     * @param entityType 엔티티 타입 (예: "설문조사", "질문")
     * @param id 엔티티 ID
     * @param operation 수행하려던 작업
     * @return 포맷된 메시지
     */
    fun formatNotFoundMessage(entityType: String, id: Any, operation: String = "조회"): String {
        val idString = when (id) {
            is String -> id
            else -> id.toString()
        }
        return entityType + "을(를) 찾을 수 없습니다. [ID: $idString, 작업: $operation]"
    }
    
    /**
     * 예외 메시지에 포함될 작업 정보를 포맷팅
     * @param operation 수행하려던 작업
     * @param entityType 대상 엔티티 타입
     * @param id 대상 엔티티 ID
     * @return 포맷된 메시지
     */
    fun formatOperationMessage(operation: String, entityType: String, id: Any): String {
        val idString = when (id) {
            is String -> id
            else -> id.toString()
        }
        return "$operation 작업을 수행할 수 없습니다. [대상: $entityType, ID: $idString]"
    }
} 