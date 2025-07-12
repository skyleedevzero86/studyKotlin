package com.kominioai.domain.survey.domain.model.service

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.global.exception.QuestionValidationException
import com.kominioai.global.exception.QuestionOptionValidationException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldThrow
import io.kotest.matchers.string.shouldContain

class QuestionValidationServiceTest : DescribeSpec({
    
    val validationService = QuestionValidationService()
    
    describe("QuestionValidationService") {
        
        describe("질문 텍스트 검증") {
            it("빈 텍스트는 검증 실패") {
                val exception = shouldThrow<QuestionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "",
                        description = null,
                        questionType = QuestionType.TEXT,
                        isRequired = false,
                        options = emptyList(),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "질문 텍스트는 비어있을 수 없습니다"
            }
            
            it("너무 긴 텍스트는 검증 실패") {
                val longText = "a".repeat(QuestionValidationService.MAX_QUESTION_TEXT_LENGTH + 1)
                val exception = shouldThrow<QuestionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = longText,
                        description = null,
                        questionType = QuestionType.TEXT,
                        isRequired = false,
                        options = emptyList(),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "질문 텍스트는 최대 ${QuestionValidationService.MAX_QUESTION_TEXT_LENGTH}자까지"
            }
            
            it("정상적인 텍스트는 검증 성공") {
                validationService.validateQuestionCreation(
                    questionText = "정상적인 질문 텍스트",
                    description = null,
                    questionType = QuestionType.TEXT,
                    isRequired = false,
                    options = emptyList(),
                    currentQuestionCount = 0
                )
            }
        }
        
        describe("질문 설명 검증") {
            it("너무 긴 설명은 검증 실패") {
                val longDescription = "a".repeat(QuestionValidationService.MAX_QUESTION_DESCRIPTION_LENGTH + 1)
                val exception = shouldThrow<QuestionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "정상적인 질문",
                        description = longDescription,
                        questionType = QuestionType.TEXT,
                        isRequired = false,
                        options = emptyList(),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "질문 설명은 최대 ${QuestionValidationService.MAX_QUESTION_DESCRIPTION_LENGTH}자까지"
            }
        }
        
        describe("선택형 질문 옵션 검증") {
            it("SINGLE_CHOICE에 옵션이 없으면 검증 실패") {
                val exception = shouldThrow<QuestionOptionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "선택형 질문",
                        description = null,
                        questionType = QuestionType.SINGLE_CHOICE,
                        isRequired = false,
                        options = emptyList(),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "선택형 질문에는 최소 ${QuestionValidationService.MIN_OPTIONS_COUNT}개 이상의 옵션이 필요합니다"
            }
            
            it("SINGLE_CHOICE에 옵션이 1개면 검증 실패") {
                val exception = shouldThrow<QuestionOptionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "선택형 질문",
                        description = null,
                        questionType = QuestionType.SINGLE_CHOICE,
                        isRequired = false,
                        options = listOf("옵션1"),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "선택형 질문에는 최소 ${QuestionValidationService.MIN_OPTIONS_COUNT}개 이상의 옵션이 필요합니다"
            }
            
            it("SINGLE_CHOICE에 옵션이 너무 많으면 검증 실패") {
                val tooManyOptions = (1..QuestionValidationService.MAX_OPTIONS_COUNT + 1).map { "옵션$it" }
                val exception = shouldThrow<QuestionOptionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "선택형 질문",
                        description = null,
                        questionType = QuestionType.SINGLE_CHOICE,
                        isRequired = false,
                        options = tooManyOptions,
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "선택형 질문의 옵션은 최대 ${QuestionValidationService.MAX_OPTIONS_COUNT}개까지"
            }
            
            it("정상적인 선택형 질문은 검증 성공") {
                validationService.validateQuestionCreation(
                    questionText = "선택형 질문",
                    description = null,
                    questionType = QuestionType.SINGLE_CHOICE,
                    isRequired = false,
                    options = listOf("옵션1", "옵션2", "옵션3"),
                    currentQuestionCount = 0
                )
            }
        }
        
        describe("주관식 질문 옵션 검증") {
            it("TEXT에 옵션이 있으면 검증 실패") {
                val exception = shouldThrow<QuestionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "주관식 질문",
                        description = null,
                        questionType = QuestionType.TEXT,
                        isRequired = false,
                        options = listOf("옵션1", "옵션2"),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "TEXT 유형의 질문에는 옵션을 설정할 수 없습니다"
            }
            
            it("정상적인 주관식 질문은 검증 성공") {
                validationService.validateQuestionCreation(
                    questionText = "주관식 질문",
                    description = null,
                    questionType = QuestionType.TEXT,
                    isRequired = false,
                    options = emptyList(),
                    currentQuestionCount = 0
                )
            }
        }
        
        describe("옵션 텍스트 검증") {
            it("빈 옵션 텍스트는 검증 실패") {
                val exception = shouldThrow<QuestionOptionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "선택형 질문",
                        description = null,
                        questionType = QuestionType.SINGLE_CHOICE,
                        isRequired = false,
                        options = listOf("옵션1", "", "옵션3"),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "2번째 옵션의 텍스트는 비어있을 수 없습니다"
            }
            
            it("너무 긴 옵션 텍스트는 검증 실패") {
                val longOption = "a".repeat(QuestionValidationService.MAX_OPTION_TEXT_LENGTH + 1)
                val exception = shouldThrow<QuestionOptionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "선택형 질문",
                        description = null,
                        questionType = QuestionType.SINGLE_CHOICE,
                        isRequired = false,
                        options = listOf("옵션1", longOption, "옵션3"),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "2번째 옵션의 텍스트는 최대 ${QuestionValidationService.MAX_OPTION_TEXT_LENGTH}자까지"
            }
            
            it("중복된 옵션은 검증 실패") {
                val exception = shouldThrow<QuestionOptionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "선택형 질문",
                        description = null,
                        questionType = QuestionType.SINGLE_CHOICE,
                        isRequired = false,
                        options = listOf("옵션1", "옵션2", "옵션1"),
                        currentQuestionCount = 0
                    )
                }
                exception.message shouldContain "중복된 옵션이 존재합니다"
            }
        }
        
        describe("설문당 질문 개수 제한 검증") {
            it("최대 질문 개수를 초과하면 검증 실패") {
                val exception = shouldThrow<QuestionValidationException> {
                    validationService.validateQuestionCreation(
                        questionText = "추가 질문",
                        description = null,
                        questionType = QuestionType.TEXT,
                        isRequired = false,
                        options = emptyList(),
                        currentQuestionCount = QuestionValidationService.MAX_QUESTIONS_PER_SURVEY
                    )
                }
                exception.message shouldContain "설문당 최대 ${QuestionValidationService.MAX_QUESTIONS_PER_SURVEY}개의 질문만 추가할 수 있습니다"
            }
        }
        
        describe("질문 순서 검증") {
            it("0 이하의 순서는 검증 실패") {
                val exception = shouldThrow<QuestionValidationException> {
                    validationService.validateQuestionOrder(0, 5)
                }
                exception.message shouldContain "질문 순서는 1 이상이어야 합니다"
            }
            
            it("현재 질문 개수 + 1을 초과하는 순서는 검증 실패") {
                val exception = shouldThrow<QuestionValidationException> {
                    validationService.validateQuestionOrder(7, 5)
                }
                exception.message shouldContain "질문 순서는 현재 질문 개수 + 1을 초과할 수 없습니다"
            }
            
            it("정상적인 순서는 검증 성공") {
                validationService.validateQuestionOrder(3, 5)
            }
        }
    }
}) 