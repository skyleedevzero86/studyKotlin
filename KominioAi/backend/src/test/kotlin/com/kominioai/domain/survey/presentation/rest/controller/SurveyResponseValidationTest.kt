package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.presentation.rest.dto.request.ValidatedSubmitResponseRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.ValidatedAnswerSubmission
import com.kominioai.global.validation.annotation.UUID
import com.kominioai.global.validation.annotation.SafeText
import com.kominioai.global.validation.annotation.ValidAnswerSubmission
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SurveyResponseValidationTest {
    
    private lateinit var validator: Validator
    
    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }
    
    @Test
    fun `유효한 UUID 형식 검증`() {
        val validUuid = "123e4567-e89b-12d3-a456-426614174000"
        val invalidUuid = "invalid-uuid"
        
        assertTrue(isValidUuid(validUuid))
        assertFalse(isValidUuid(invalidUuid))
    }
    
    @Test
    fun `안전한 텍스트 검증 - XSS 공격 방지`() {
        val safeText = "안전한 답변입니다"
        val xssText = "<script>alert('XSS')</script>"
        val sqlInjectionText = "'; DROP TABLE users; --"
        
        assertTrue(isValidSafeText(safeText))
        assertFalse(isValidSafeText(xssText))
        assertFalse(isValidSafeText(sqlInjectionText))
    }
    
    @Test
    fun `유효한 답변 제출 요청 검증`() {
        val validRequest = ValidatedSubmitResponseRequest(
            surveyId = "123e4567-e89b-12d3-a456-426614174000",
            answers = listOf(
                ValidatedAnswerSubmission(
                    questionId = "456e7890-e89b-12d3-a456-426614174001",
                    answerText = "안전한 답변",
                    selectedOptionIds = emptyList()
                )
            )
        )
        
        val violations = validator.validate(validRequest)
        assertTrue(violations.isEmpty(), "유효한 요청에서 검증 오류가 발생했습니다")
    }
    
    @Test
    fun `무효한 설문지 ID 검증`() {
        val invalidRequest = ValidatedSubmitResponseRequest(
            surveyId = "invalid-uuid",
            answers = listOf(
                ValidatedAnswerSubmission(
                    questionId = "456e7890-e89b-12d3-a456-426614174001",
                    answerText = "안전한 답변",
                    selectedOptionIds = emptyList()
                )
            )
        )
        
        val violations = validator.validate(invalidRequest)
        assertFalse(violations.isEmpty(), "무효한 설문지 ID가 검증을 통과했습니다")
    }
    
    @Test
    fun `빈 답변 목록 검증`() {
        val invalidRequest = ValidatedSubmitResponseRequest(
            surveyId = "123e4567-e89b-12d3-a456-426614174000",
            answers = emptyList()
        )
        
        val violations = validator.validate(invalidRequest)
        assertFalse(violations.isEmpty(), "빈 답변 목록이 검증을 통과했습니다")
    }
    
    @Test
    fun `중복 질문 ID 검증`() {
        val invalidRequest = ValidatedSubmitResponseRequest(
            surveyId = "123e4567-e89b-12d3-a456-426614174000",
            answers = listOf(
                ValidatedAnswerSubmission(
                    questionId = "456e7890-e89b-12d3-a456-426614174001",
                    answerText = "첫 번째 답변",
                    selectedOptionIds = emptyList()
                ),
                ValidatedAnswerSubmission(
                    questionId = "456e7890-e89b-12d3-a456-426614174001", // 중복
                    answerText = "두 번째 답변",
                    selectedOptionIds = emptyList()
                )
            )
        )
        
        assertThrows<IllegalArgumentException> {
            // init 블록에서 중복 검증이 발생
            invalidRequest
        }
    }
    
    @Test
    fun `XSS 공격 텍스트 검증`() {
        val xssAnswers = listOf(
            ValidatedAnswerSubmission(
                questionId = "456e7890-e89b-12d3-a456-426614174001",
                answerText = "<script>alert('XSS')</script>",
                selectedOptionIds = emptyList()
            )
        )
        
        val violations = validator.validate(ValidatedSubmitResponseRequest(
            surveyId = "123e4567-e89b-12d3-a456-426614174000",
            answers = xssAnswers
        ))
        
        assertFalse(violations.isEmpty(), "XSS 공격 텍스트가 검증을 통과했습니다")
    }
    
    @Test
    fun `SQL Injection 공격 텍스트 검증`() {
        val sqlInjectionAnswers = listOf(
            ValidatedAnswerSubmission(
                questionId = "456e7890-e89b-12d3-a456-426614174001",
                answerText = "'; DROP TABLE users; --",
                selectedOptionIds = emptyList()
            )
        )
        
        val violations = validator.validate(ValidatedSubmitResponseRequest(
            surveyId = "123e4567-e89b-12d3-a456-426614174000",
            answers = sqlInjectionAnswers
        ))
        
        assertFalse(violations.isEmpty(), "SQL Injection 공격 텍스트가 검증을 통과했습니다")
    }
    
    @Test
    fun `답변 텍스트 길이 제한 검증`() {
        val longText = "a".repeat(2001) // 2000자 초과
        val longTextAnswers = listOf(
            ValidatedAnswerSubmission(
                questionId = "456e7890-e89b-12d3-a456-426614174001",
                answerText = longText,
                selectedOptionIds = emptyList()
            )
        )
        
        val violations = validator.validate(ValidatedSubmitResponseRequest(
            surveyId = "123e4567-e89b-12d3-a456-426614174000",
            answers = longTextAnswers
        ))
        
        assertFalse(violations.isEmpty(), "길이 제한을 초과한 텍스트가 검증을 통과했습니다")
    }
    
    @Test
    fun `선택된 옵션 개수 제한 검증`() {
        val manyOptions = (1..11).map { "option-$it" } // 10개 초과
        val manyOptionsAnswers = listOf(
            ValidatedAnswerSubmission(
                questionId = "456e7890-e89b-12d3-a456-426614174001",
                answerText = null,
                selectedOptionIds = manyOptions
            )
        )
        
        val violations = validator.validate(ValidatedSubmitResponseRequest(
            surveyId = "123e4567-e89b-12d3-a456-426614174000",
            answers = manyOptionsAnswers
        ))
        
        assertFalse(violations.isEmpty(), "옵션 개수 제한을 초과한 요청이 검증을 통과했습니다")
    }
    
    @Test
    fun `답변과 옵션 모두 없는 경우 검증`() {
        val emptyAnswer = ValidatedAnswerSubmission(
            questionId = "456e7890-e89b-12d3-a456-426614174001",
            answerText = null,
            selectedOptionIds = emptyList()
        )
        
        assertThrows<IllegalArgumentException> {
            // init 블록에서 검증이 발생
            emptyAnswer
        }
    }
    
    private fun isValidUuid(uuid: String): Boolean {
        return uuid.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
    }
    
    private fun isValidSafeText(text: String): Boolean {
        val violations = validator.validateValue(ValidatedAnswerSubmission::class.java, "answerText", text)
        return violations.isEmpty()
    }
} 