package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.SurveyUseCase
import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.Answer
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import com.kominioai.global.exception.ExceptionUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
@Transactional
class SurveyApplicationService(
    private val surveyRepository: SurveyRepository,
    private val surveyResponseRepository: SurveyResponseRepository,
    private val eventPublisher: EventPublisher
) : SurveyUseCase {

    override fun createSurvey(command: CreateSurveyCommand): Mono<SurveyId> {

        validateCreateSurveyCommand(command)

        val survey = Survey.create(
            title = command.title,
            description = command.description,
            createdBy = command.createdBy,
            settings = command.settings
        )

        return surveyRepository.save(survey)
            .flatMap { savedSurvey ->
                eventPublisher.publishReactive(
                    com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyCreated(
                        surveyId = savedSurvey.id,
                        title = savedSurvey.title,
                        description = savedSurvey.description,
                        createdBy = savedSurvey.createdBy,
                        settings = mapOf(
                            "allowAnonymous" to savedSurvey.settings.allowAnonymous,
                            "allowMultipleResponses" to savedSurvey.settings.allowMultipleResponses,
                            "requireLogin" to savedSurvey.settings.requireLogin,
                            "collectIpAddress" to savedSurvey.settings.collectIpAddress
                        )
                    )
                ).thenReturn(savedSurvey.id)
            }
    }

    override fun addQuestion(command: AddQuestionCommand): Mono<QuestionId> {
        return surveyRepository.findById(command.surveyId)
            .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(command.surveyId, "질문 추가")))
            .flatMap { survey ->
                val question = Question.create(
                    surveyId = command.surveyId,
                    order = command.order,
                    text = command.text,
                    description = command.description,
                    type = command.type,
                    required = command.required,
                    options = command.options
                )

                val updatedSurvey = survey.addQuestion(question)

                surveyRepository.save(updatedSurvey)
                    .flatMap { savedSurvey ->
                        eventPublisher.publishReactive(
                            com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionAdded(
                                surveyId = savedSurvey.id,
                                questionId = question.id,
                                questionText = question.text,
                                questionType = question.type.name,
                                order = question.order,
                                addedBy = savedSurvey.createdBy
                            )
                        ).thenReturn(question.id)
                    }
            }
    }

    override fun publishSurvey(command: PublishSurveyCommand): Mono<Void> {
        return surveyRepository.findById(command.surveyId)
            .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(command.surveyId, "설문조사 게시")))
            .flatMap { survey ->

                val publishedSurvey = survey.publish()

                surveyRepository.save(publishedSurvey)
                    .flatMap { savedSurvey ->

                        eventPublisher.publishReactive(
                            com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyPublished(
                                surveyId = savedSurvey.id,
                                publishedBy = command.userId,
                                questionCount = savedSurvey.questions.size
                            )
                        ).then()
                    }
            }
    }

    override fun submitResponse(command: SubmitResponseCommand): Mono<ResponseId> {
        return surveyRepository.findById(command.surveyId)
            .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(command.surveyId, "응답 제출")))
            .flatMap { survey ->

                if (survey.status != SurveyStatus.PUBLISHED) {
                    return@flatMap Mono.error<ResponseId>(
                        ExceptionUtils.createInvalidSurveyOperationException(
                            surveyId = command.surveyId,
                            operation = "응답 제출",
                            reason = "게시된 설문조사만 응답할 수 있습니다"
                        )
                    )
                }

                validateAnswers(survey.questions, command.answers)

                val response = SurveyResponse.create(
                    surveyId = command.surveyId,
                    respondentId = command.respondentId?.let { UserId.from(it) },
                    answers = command.answers,
                    ipAddress = command.ipAddress
                )

                surveyResponseRepository.save(response)
                    .flatMap { savedResponse ->
                        eventPublisher.publishReactive(
                            com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseSubmitted(
                                surveyId = savedResponse.surveyId,
                                responseId = savedResponse.id,
                                respondentId = savedResponse.respondentId,
                                ipAddress = savedResponse.ipAddress,
                                answerCount = savedResponse.answers.size
                            )
                        ).thenReturn(ResponseId.from(savedResponse.id.value))
                    }
            }
    }

    override fun getSurvey(id: SurveyId): Mono<SurveyDto> {
        return surveyRepository.findByIdWithQuestions(id)
            .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(id, "설문조사 조회")))
            .map { SurveyDto.from(it) }
    }

    override fun getAllSurveys(pageable: Pageable): Mono<Page<SurveyDto>> {
        return surveyRepository.findAllWithPaging(pageable)
            .map { page ->
                val surveyDtos = page.content.map { SurveyDto.from(it) }
                org.springframework.data.domain.PageImpl(surveyDtos, pageable, page.totalElements)
            }
    }

    override fun getSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto> {
        return Mono.zip(
            surveyRepository.findByIdWithQuestions(surveyId)
                .switchIfEmpty(Mono.error(ExceptionUtils.createSurveyNotFoundException(surveyId, "통계 조회"))),
            surveyResponseRepository.countBySurveyId(surveyId)
        ).map { tuple ->
            val survey = tuple.t1
            val responseCount = tuple.t2

            SurveyStatisticsDto(
                surveyId = survey.id,
                title = survey.title,
                responseCount = responseCount.toInt(),
                questionStatistics = calculateQuestionStatistics(survey.questions, responseCount)
            )
        }
    }

    override fun getSurveysByStatus(status: SurveyStatus, pageable: Pageable): Mono<Page<SurveyDto>> {
        return surveyRepository.findByStatusWithPaging(status, pageable)
            .map { page ->
                val surveyDtos = page.content.map { SurveyDto.from(it) }
                org.springframework.data.domain.PageImpl(surveyDtos, pageable, page.totalElements)
            }
    }

    override fun getSurveysByUser(userId: UserId, pageable: Pageable): Mono<Page<SurveyDto>> {
        return surveyRepository.findByCreatedByWithPaging(userId, pageable)
            .map { page ->
                val surveyDtos = page.content.map { SurveyDto.from(it) }
                org.springframework.data.domain.PageImpl(surveyDtos, pageable, page.totalElements)
            }
    }

    override fun getPublishedSurveys(pageable: Pageable): Mono<Page<SurveyDto>> {
        return surveyRepository.findPublishedSurveysWithPaging(pageable)
            .map { page ->
                val surveyDtos = page.content.map { SurveyDto.from(it) }
                org.springframework.data.domain.PageImpl(surveyDtos, pageable, page.totalElements)
            }
    }

    private fun validateCreateSurveyCommand(command: CreateSurveyCommand) {
        require(command.title.isNotBlank()) { "설문조사 제목은 필수입니다." }
        require(command.title.length <= 200) { "설문조사 제목은 200자를 초과할 수 없습니다." }
        command.description?.let { desc ->
            require(desc.length <= 1000) { "설문조사 설명은 1000자를 초과할 수 없습니다." }
        }
    }

    private fun validateAnswers(questions: List<Question>, answers: List<Answer>) {
        val questionMap = questions.associateBy { it.id }
        val answerMap = answers.associateBy { it.questionId }

        questions.filter { it.required }.forEach { question ->
            if (!answerMap.containsKey(question.id)) {
                throw IllegalArgumentException("필수 질문에 대한 답변이 누락되었습니다: ${question.text}")
            }
        }

        answers.forEach { answer ->
            val question = questionMap[answer.questionId]
                ?: throw ExceptionUtils.createQuestionNotFoundException(answer.questionId, "응답 제출")

            if (!question.validateAnswer(answer)) {
                throw IllegalArgumentException("질문 '${question.text}'에 대한 답변이 유효하지 않습니다.")
            }
        }
    }

    private fun calculateQuestionStatistics(questions: List<Question>, totalResponses: Long): List<com.kominioai.domain.survey.presentation.rest.dto.response.QuestionStatisticsDto> {

        return questions.map { question ->
            com.kominioai.domain.survey.presentation.rest.dto.response.QuestionStatisticsDto(
                questionId = question.id,
                text = question.text,
                type = question.type,
                totalAnswers = totalResponses.toInt(),
                optionStatistics = question.options.map { option ->
                    com.kominioai.domain.survey.presentation.rest.dto.response.OptionStatisticsDto(
                        optionId = option.id,
                        text = option.text,
                        count = 0
                    )
                }
            )
        }
    }
}