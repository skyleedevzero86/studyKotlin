package com.kominioai.global.exception

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("ExceptionUtils 테스트")
class ExceptionUtilsTest {

    @Test
    @DisplayName("SurveyNotFoundException을 올바른 형식으로 생성해야 한다")
    fun `should create SurveyNotFoundException with correct format`() {
        val surveyId = "test-survey-123"
        val operation = "질문 추가"

        val exception = ExceptionUtils.createSurveyNotFoundException(surveyId, operation)

        assertThat(exception).isInstanceOf(SurveyNotFoundException::class.java)
        assertThat(exception.message).isEqualTo("설문조사를 찾을 수 없습니다. [ID: $surveyId, 작업: $operation]")
    }

    @Test
    @DisplayName("SurveyId 객체로 SurveyNotFoundException을 생성할 수 있어야 한다")
    fun `should create SurveyNotFoundException with SurveyId object`() {
        val surveyId = SurveyId.from("test-survey-456")
        val operation = "설문조사 조회"

        val exception = ExceptionUtils.createSurveyNotFoundException(surveyId, operation)

        assertThat(exception.message).isEqualTo("설문조사를 찾을 수 없습니다. [ID: ${surveyId.value}, 작업: $operation]")
    }

    @Test
    @DisplayName("QuestionNotFoundException을 올바른 형식으로 생성해야 한다")
    fun `should create QuestionNotFoundException with correct format`() {
        val questionId = "test-question-789"
        val operation = "응답 제출"

        val exception = ExceptionUtils.createQuestionNotFoundException(questionId, operation)

        assertThat(exception).isInstanceOf(QuestionNotFoundException::class.java)
        assertThat(exception.message).isEqualTo("질문을 찾을 수 없습니다. [ID: $questionId, 작업: $operation]")
    }

    @Test
    @DisplayName("QuestionId 객체로 QuestionNotFoundException을 생성할 수 있어야 한다")
    fun `should create QuestionNotFoundException with QuestionId object`() {
        val questionId = QuestionId.from("test-question-101")
        val operation = "답변 검증"

        val exception = ExceptionUtils.createQuestionNotFoundException(questionId, operation)

        assertThat(exception.message).isEqualTo("질문을 찾을 수 없습니다. [ID: ${questionId.value}, 작업: $operation]")
    }

    @Test
    @DisplayName("UserNotFoundException을 올바른 형식으로 생성해야 한다")
    fun `should create UserNotFoundException with correct format`() {
        val userId = "test-user-202"
        val operation = "사용자 정보 조회"

        val exception = ExceptionUtils.createUserNotFoundException(userId, operation)

        assertThat(exception).isInstanceOf(UserNotFoundException::class.java)
        assertThat(exception.message).isEqualTo("사용자를 찾을 수 없습니다. [ID: $userId, 작업: $operation]")
    }

    @Test
    @DisplayName("UserId 객체로 UserNotFoundException을 생성할 수 있어야 한다")
    fun `should create UserNotFoundException with UserId object`() {
        val userId = UserId.from("test-user-303")
        val operation = "권한 확인"

        val exception = ExceptionUtils.createUserNotFoundException(userId, operation)

        assertThat(exception.message).isEqualTo("사용자를 찾을 수 없습니다. [ID: ${userId.value}, 작업: $operation]")
    }

    @Test
    @DisplayName("SurveyResponseNotFoundException을 올바른 형식으로 생성해야 한다")
    fun `should create SurveyResponseNotFoundException with correct format`() {
        val responseId = "test-response-404"
        val operation = "응답 분석"

        val exception = ExceptionUtils.createSurveyResponseNotFoundException(responseId, operation)

        assertThat(exception).isInstanceOf(SurveyResponseNotFoundException::class.java)
        assertThat(exception.message).isEqualTo("설문 응답을 찾을 수 없습니다. [ID: $responseId, 작업: $operation]")
    }

    @Test
    @DisplayName("ResponseId 객체로 SurveyResponseNotFoundException을 생성할 수 있어야 한다")
    fun `should create SurveyResponseNotFoundException with ResponseId object`() {
        val responseId = ResponseId.from("test-response-505")
        val operation = "응답 수정"

        val exception = ExceptionUtils.createSurveyResponseNotFoundException(responseId, operation)

        assertThat(exception.message).isEqualTo("설문 응답을 찾을 수 없습니다. [ID: ${responseId.value}, 작업: $operation]")
    }

    @Test
    @DisplayName("InvalidSurveyOperationException을 올바른 형식으로 생성해야 한다")
    fun `should create InvalidSurveyOperationException with correct format`() {
        val surveyId = "test-survey-606"
        val operation = "응답 제출"
        val reason = "게시된 설문조사만 응답할 수 있습니다"

        val exception = ExceptionUtils.createInvalidSurveyOperationException(surveyId, operation, reason)

        assertThat(exception).isInstanceOf(InvalidSurveyOperationException::class.java)
        assertThat(exception.message).isEqualTo("설문조사 작업을 수행할 수 없습니다. [ID: $surveyId, 작업: $operation, 이유: $reason]")
    }

    @Test
    @DisplayName("SurveyId 객체로 InvalidSurveyOperationException을 생성할 수 있어야 한다")
    fun `should create InvalidSurveyOperationException with SurveyId object`() {
        val surveyId = SurveyId.from("test-survey-707")
        val operation = "설문조사 수정"
        val reason = "이미 게시된 설문조사는 수정할 수 없습니다"

        val exception = ExceptionUtils.createInvalidSurveyOperationException(surveyId, operation, reason)

        assertThat(exception.message).isEqualTo("설문조사 작업을 수행할 수 없습니다. [ID: ${surveyId.value}, 작업: $operation, 이유: $reason]")
    }

    @Test
    @DisplayName("SurveyValidationException을 올바른 형식으로 생성해야 한다")
    fun `should create SurveyValidationException with correct format`() {
        val field = "질문 유형"
        val value = QuestionType.SINGLE_CHOICE
        val reason = "질문 유형과 답변 유형이 일치하지 않습니다"

        val exception = ExceptionUtils.createSurveyValidationException(field, value, reason)

        assertThat(exception).isInstanceOf(SurveyValidationException::class.java)
        assertThat(exception.message).isEqualTo("설문조사 검증 실패. [필드: $field, 값: $value, 이유: $reason]")
    }

    @Test
    @DisplayName("null 값으로 SurveyValidationException을 생성할 수 있어야 한다")
    fun `should create SurveyValidationException with null value`() {
        val field = "설문 제목"
        val value: String? = null
        val reason = "설문 제목은 필수입니다"

        val exception = ExceptionUtils.createSurveyValidationException(field, value, reason)

        assertThat(exception.message).isEqualTo("설문조사 검증 실패. [필드: $field, 값: null, 이유: $reason]")
    }

    @Test
    @DisplayName("기본 작업으로 예외를 생성할 수 있어야 한다")
    fun `should create exception with default operation`() {
        val surveyId = "test-survey-808"

        val exception = ExceptionUtils.createSurveyNotFoundException(surveyId)

        assertThat(exception.message).isEqualTo("설문조사를 찾을 수 없습니다. [ID: $surveyId, 작업: 조회]")
    }

    @Test
    @DisplayName("formatNotFoundMessage가 올바른 형식으로 메시지를 생성해야 한다")
    fun `should format not found message correctly`() {
        val entityType = "설문조사"
        val id = "test-id-909"
        val operation = "삭제"

        val message = ExceptionUtils.formatNotFoundMessage(entityType, id, operation)

        assertThat(message).isEqualTo("$entityType을(를) 찾을 수 없습니다. [ID: $id, 작업: $operation]")
    }

    @Test
    @DisplayName("formatOperationMessage가 올바른 형식으로 메시지를 생성해야 한다")
    fun `should format operation message correctly`() {
        val operation = "수정"
        val entityType = "질문"
        val id = "test-question-1010"

        val message = ExceptionUtils.formatOperationMessage(operation, entityType, id)

        assertThat(message).isEqualTo("$operation 작업을 수행할 수 없습니다. [대상: $entityType, ID: $id]")
    }

    @Test
    @DisplayName("다양한 타입의 ID로 예외를 생성할 수 있어야 한다")
    fun `should create exception with various ID types`() {
        val stringId = "string-id"
        val intId = 123
        val longId = 456L

        val stringException = ExceptionUtils.createSurveyNotFoundException(stringId, "테스트")
        assertThat(stringException.message).contains("string-id")

        val intException = ExceptionUtils.createSurveyNotFoundException(intId, "테스트")
        assertThat(intException.message).contains("123")

        val longException = ExceptionUtils.createSurveyNotFoundException(longId, "테스트")
        assertThat(longException.message).contains("456")
    }
}