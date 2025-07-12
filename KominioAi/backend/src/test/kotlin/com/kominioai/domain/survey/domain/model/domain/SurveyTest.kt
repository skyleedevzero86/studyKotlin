package com.kominioai.domain.survey.domain.model.domain

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.service.QuestionValidationService
import com.kominioai.global.exception.QuestionValidationException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldThrow
import io.kotest.matchers.string.shouldContain

class SurveyTest : DescribeSpec({
    
    val validationService = QuestionValidationService()
    val userId = UserId.from("test-user-id")
    
    describe("Survey 도메인 모델") {
        
        describe("create 메서드") {
            it("정상적인 설문 생성") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = "테스트 설명",
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                survey.title shouldBe "테스트 설문"
                survey.description shouldBe "테스트 설명"
                survey.createdBy shouldBe userId
                survey.status shouldBe SurveyStatus.DRAFT
                survey.questions shouldBe emptyList()
            }
        }
        
        describe("addQuestion 메서드") {
            val survey = Survey.create(
                title = "테스트 설문",
                description = null,
                createdBy = userId,
                settings = com.kominioai.domain.survey.domain.model.SurveySettings()
            )
            
            it("정상적인 질문 추가") {
                val question = Question.create(
                    surveyId = survey.id,
                    order = 1,
                    text = "테스트 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList(),
                    validationService = validationService
                )
                
                val updatedSurvey = survey.addQuestion(question, validationService)
                
                updatedSurvey.questions.size shouldBe 1
                updatedSurvey.questions.first().text shouldBe "테스트 질문"
                updatedSurvey.questions.first().order shouldBe 1
            }
            
            it("게시된 설문에 질문 추가 시 예외 발생") {
                val publishedSurvey = survey.copy(status = SurveyStatus.PUBLISHED)
                val question = Question.create(
                    surveyId = survey.id,
                    order = 1,
                    text = "테스트 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList()
                )
                
                val exception = shouldThrow<QuestionValidationException> {
                    publishedSurvey.addQuestion(question, validationService)
                }
                exception.message shouldContain "게시된 설문조사에는 질문을 추가할 수 없습니다"
            }
            
            it("종료된 설문에 질문 추가 시 예외 발생") {
                val closedSurvey = survey.copy(status = SurveyStatus.CLOSED)
                val question = Question.create(
                    surveyId = survey.id,
                    order = 1,
                    text = "테스트 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList()
                )
                
                val exception = shouldThrow<QuestionValidationException> {
                    closedSurvey.addQuestion(question, validationService)
                }
                exception.message shouldContain "종료된 설문조사에는 질문을 추가할 수 없습니다"
            }
            
            it("중복된 순서의 질문 추가 시 자동 순서 조정") {
                val question1 = Question.create(
                    surveyId = survey.id,
                    order = 1,
                    text = "첫 번째 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList()
                )
                
                val question2 = Question.create(
                    surveyId = survey.id,
                    order = 1, // 중복된 순서
                    text = "두 번째 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList()
                )
                
                val surveyWithFirstQuestion = survey.addQuestion(question1, validationService)
                val surveyWithBothQuestions = surveyWithFirstQuestion.addQuestion(question2, validationService)
                
                surveyWithBothQuestions.questions.size shouldBe 2
                surveyWithBothQuestions.questions[0].order shouldBe 1
                surveyWithBothQuestions.questions[1].order shouldBe 2 // 자동으로 2로 조정됨
            }
        }
        
        describe("publish 메서드") {
            it("정상적인 설문 게시") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                val question = Question.create(
                    surveyId = survey.id,
                    order = 1,
                    text = "테스트 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList(),
                    validationService = validationService
                )
                
                val surveyWithQuestion = survey.addQuestion(question, validationService)
                val publishedSurvey = surveyWithQuestion.publish()
                
                publishedSurvey.status shouldBe SurveyStatus.PUBLISHED
            }
            
            it("질문이 없는 설문 게시 시 예외 발생") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                val exception = shouldThrow<QuestionValidationException> {
                    survey.publish()
                }
                exception.message shouldContain "질문이 없는 설문조사는 게시할 수 없습니다"
            }
            
            it("유효하지 않은 질문이 있는 설문 게시 시 예외 발생") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                val invalidQuestion = Question(
                    id = com.kominioai.domain.survey.domain.valueobject.QuestionId.from("test-id"),
                    surveyId = survey.id,
                    order = 1,
                    text = "", // 빈 텍스트로 유효하지 않음
                    description = null,
                    type = QuestionType.SINGLE_CHOICE,
                    required = false,
                    options = emptyList() // SINGLE_CHOICE에 옵션이 없어서 유효하지 않음
                )
                
                val surveyWithInvalidQuestion = survey.copy(questions = listOf(invalidQuestion))
                
                val exception = shouldThrow<QuestionValidationException> {
                    surveyWithInvalidQuestion.publish()
                }
                exception.message shouldContain "유효하지 않은 질문이 포함된 설문조사는 게시할 수 없습니다"
            }
            
            it("필수 선택형 질문에 옵션이 부족한 설문 게시 시 예외 발생") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                val questionWithInsufficientOptions = Question.create(
                    surveyId = survey.id,
                    order = 1,
                    text = "필수 선택형 질문",
                    description = null,
                    type = QuestionType.SINGLE_CHOICE,
                    required = true,
                    options = listOf("옵션1"), // 1개만 있어서 부족
                    validationService = validationService
                )
                
                val surveyWithQuestion = survey.addQuestion(questionWithInsufficientOptions, validationService)
                
                val exception = shouldThrow<QuestionValidationException> {
                    surveyWithQuestion.publish()
                }
                exception.message shouldContain "필수 선택형 질문에는 최소 2개 이상의 옵션이 필요합니다"
            }
        }
        
        describe("canBePublished 메서드") {
            it("게시 가능한 설문은 true 반환") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                val question = Question.create(
                    surveyId = survey.id,
                    order = 1,
                    text = "테스트 질문",
                    description = null,
                    type = QuestionType.TEXT,
                    required = false,
                    options = emptyList(),
                    validationService = validationService
                )
                
                val surveyWithQuestion = survey.addQuestion(question, validationService)
                surveyWithQuestion.canBePublished() shouldBe true
            }
            
            it("게시 불가능한 설문은 false 반환") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                survey.canBePublished() shouldBe false
            }
        }
        
        describe("canAddQuestions 메서드") {
            it("질문 추가 가능한 설문은 true 반환") {
                val survey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                )
                
                survey.canAddQuestions() shouldBe true
            }
            
            it("질문 추가 불가능한 설문은 false 반환") {
                val publishedSurvey = Survey.create(
                    title = "테스트 설문",
                    description = null,
                    createdBy = userId,
                    settings = com.kominioai.domain.survey.domain.model.SurveySettings()
                ).copy(status = SurveyStatus.PUBLISHED)
                
                publishedSurvey.canAddQuestions() shouldBe false
            }
        }
    }
}) 