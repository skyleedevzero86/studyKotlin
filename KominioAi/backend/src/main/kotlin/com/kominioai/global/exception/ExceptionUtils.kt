package com.kominioai.global.exception

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import org.slf4j.LoggerFactory

object ExceptionUtils {
    
    private val logger = LoggerFactory.getLogger(ExceptionUtils::class.java)

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

    fun createSurveyValidationException(field: String, value: Any? = null, reason: String): SurveyValidationException {
        val valueString = value?.toString() ?: "null"
        val message = "설문조사 검증 실패. [필드: $field, 값: $valueString, 이유: $reason]"
        
        logger.warn("SurveyValidationException 발생: $message")
        return SurveyValidationException(message)
    }

    fun formatNotFoundMessage(entityType: String, id: Any, operation: String = "조회"): String {
        val idString = when (id) {
            is String -> id
            else -> id.toString()
        }
        return entityType + "을(를) 찾을 수 없습니다. [ID: $idString, 작업: $operation]"
    }

    fun formatOperationMessage(operation: String, entityType: String, id: Any): String {
        val idString = when (id) {
            is String -> id
            else -> id.toString()
        }
        return "$operation 작업을 수행할 수 없습니다. [대상: $entityType, ID: $idString]"
    }
} 