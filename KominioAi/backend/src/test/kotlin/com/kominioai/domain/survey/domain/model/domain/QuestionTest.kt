package com.kominioai.domain.survey.domain.model.domain

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.service.QuestionValidationService
import com.kominioai.global.exception.QuestionValidationException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldThrow
import io.kotest.matchers.string.shouldContain

class QuestionTest : DescribeSpec({
    
    val validationService = QuestionValidationService()
    val surveyId = SurveyId.from("test-survey-id")
    
    describe("Question 도메인 모델") {
        
        describe("create 메서드") {
            
            it("validationService 없이 기본 검증만 수행") {
                val question = Question.create(
                    surveyId = surveyId,
                    order = 1,
                    text = "정상적인 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList()
                )
                
                question.text shouldBe "정상적인 질문"
                question.type shouldBe QuestionType.TEXT
                question.options shouldBe emptyList()
            }
            
            it("빈 텍스트로 생성 시 예외 발생") {
                val exception = shouldThrow<QuestionValidationException> {
                    Question.create(
                        surveyId = surveyId,
                        order = 1,
                        text = "",
                        description = null,
                        type = QuestionType.TEXT,
                        required = false,
                        options = emptyList()
                    )
                }
                exception.message shouldContain "질문 텍스트는 비어있을 수 없습니다"
            }
            
            it("SINGLE_CHOICE에 옵션이 없으면 예외 발생") {
                val exception = shouldThrow<QuestionValidationException> {
                    Question.create(
                        surveyId = surveyId,
                        order = 1,
                        text = "선택형 질문",
                        description = null,
                        type = QuestionType.SINGLE_CHOICE,
                        required = false,
                        options = emptyList()
                    )
                }
                exception.message shouldContain "SINGLE_CHOICE 유형의 질문에는 옵션이 필요합니다"
            }
            
            it("TEXT에 옵션이 있으면 예외 발생") {
                val exception = shouldThrow<QuestionValidationException> {
                    Question.create(
                        surveyId = surveyId,
                        order = 1,
                        text = "주관식 질문",
                        description = null,
                        type = QuestionType.TEXT,
                        required = false,
                        options = listOf("옵션1", "옵션2")
                    )
                }
                exception.message shouldContain "TEXT 유형의 질문에는 옵션을 설정할 수 없습니다"
            }
            
            it("validationService와 함께 강화된 검증 수행") {
                val question = Question.create(
                    surveyId = surveyId,
                    order = 1,
                    text = "정상적인 선택형 질문",
                    description = "질문 설명",
                    type = QuestionType.SINGLE_CHOICE,
                    required = true,
                    options = listOf("옵션1", "옵션2", "옵션3"),
                    validationService = validationService
                )
                
                question.text shouldBe "정상적인 선택형 질문"
                question.description shouldBe "질문 설명"
                question.type shouldBe QuestionType.SINGLE_CHOICE
                question.required shouldBe true
                question.options.size shouldBe 3
            }
            
            it("validationService와 함께 검증 실패 시 예외 발생") {
                val exception = shouldThrow<QuestionValidationException> {
                    Question.create(
                        surveyId = surveyId,
                        order = 1,
                        text = "a".repeat(QuestionValidationService.MAX_QUESTION_TEXT_LENGTH + 1),
                        description = null,
                        type = QuestionType.TEXT,
                        required = false,
                        options = emptyList(),
                        validationService = validationService
                    )
                }
                exception.message shouldContain "질문 텍스트는 최대 ${QuestionValidationService.MAX_QUESTION_TEXT_LENGTH}자까지"
            }
        }
        
        describe("validateModification 메서드") {
            val existingQuestion = Question.create(
                surveyId = surveyId,
                order = 1,
                text = "기존 질문",
                description = null,
                type = QuestionType.SINGLE_CHOICE,
                required = false,
                options = listOf("기존옵션1", "기존옵션2"),
                validationService = validationService
            )
            
            it("정상적인 수정은 검증 성공") {
                existingQuestion.validateModification(
                    newText = "수정된 질문",
                    newDescription = "수정된 설명",
                    newType = QuestionType.SINGLE_CHOICE,
                    newRequired = true,
                    newOptions = listOf("새옵션1", "새옵션2", "새옵션3"),
                    validationService = validationService
                )
            }
            
            it("수정 시 검증 실패하면 예외 발생") {
                val exception = shouldThrow<QuestionValidationException> {
                    existingQuestion.validateModification(
                        newText = "",
                        newDescription = null,
                        newType = QuestionType.SINGLE_CHOICE,
                        newRequired = false,
                        newOptions = listOf("옵션1"),
                        validationService = validationService
                    )
                }
                exception.message shouldContain "질문 텍스트는 비어있을 수 없습니다"
            }
        }
        
        describe("isValid 메서드") {
            it("유효한 질문은 true 반환") {
                val question = Question.create(
                    surveyId = surveyId,
                    order = 1,
                    text = "유효한 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList()
                )
                
                question.isValid() shouldBe true
            }
            
            it("유효하지 않은 질문은 false 반환") {
                val question = Question(
                    id = com.kominioai.domain.survey.domain.valueobject.QuestionId.from("test-id"),
                    surveyId = surveyId,
                    order = 1,
                    text = "", // 빈 텍스트로 유효하지 않음
                    description = null,
                    type = QuestionType.SINGLE_CHOICE,
                    required = false,
                    options = emptyList() // SINGLE_CHOICE에 옵션이 없어서 유효하지 않음
                )
                
                question.isValid() shouldBe false
            }
        }
        
        describe("validateAnswer 메서드") {
            val textQuestion = Question.create(
                surveyId = surveyId,
                order = 1,
                text = "텍스트 질문",
                description = null,
                type = QuestionType.TEXT,
                required = true,
                options = emptyList()
            )
            
            val singleChoiceQuestion = Question.create(
                surveyId = surveyId,
                order = 2,
                text = "단일 선택 질문",
                description = null,
                type = QuestionType.SINGLE_CHOICE,
                required = true,
                options = listOf("옵션1", "옵션2", "옵션3"),
                validationService = validationService
            )
            
            it("TEXT 질문에 텍스트 답변이 있으면 유효") {
                val answer = Answer.create(
                    responseId = "response-1",
                    questionId = textQuestion.id,
                    questionType = QuestionType.TEXT,
                    textAnswer = "텍스트 답변",
                    selectedOptions = emptyList()
                )
                
                textQuestion.validateAnswer(answer) shouldBe true
            }
            
            it("TEXT 질문에 텍스트 답변이 없으면 무효") {
                val answer = Answer.create(
                    responseId = "response-1",
                    questionId = textQuestion.id,
                    questionType = QuestionType.TEXT,
                    textAnswer = "",
                    selectedOptions = emptyList()
                )
                
                textQuestion.validateAnswer(answer) shouldBe false
            }
            
            it("SINGLE_CHOICE 질문에 정확히 1개 옵션 선택하면 유효") {
                val answer = Answer.create(
                    responseId = "response-1",
                    questionId = singleChoiceQuestion.id,
                    questionType = QuestionType.SINGLE_CHOICE,
                    textAnswer = null,
                    selectedOptions = listOf(singleChoiceQuestion.options.first())
                )
                
                singleChoiceQuestion.validateAnswer(answer) shouldBe true
            }
            
            it("SINGLE_CHOICE 질문에 옵션을 선택하지 않으면 무효") {
                val answer = Answer.create(
                    responseId = "response-1",
                    questionId = singleChoiceQuestion.id,
                    questionType = QuestionType.SINGLE_CHOICE,
                    textAnswer = null,
                    selectedOptions = emptyList()
                )
                
                singleChoiceQuestion.validateAnswer(answer) shouldBe false
            }
        }
    }
}) 