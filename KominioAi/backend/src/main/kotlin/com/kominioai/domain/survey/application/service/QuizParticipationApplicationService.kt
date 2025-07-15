package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.*
import com.kominioai.domain.survey.application.port.`in`.*
import com.kominioai.domain.survey.application.port.out.*
import com.kominioai.domain.survey.domain.event.QuizParticipationEvent
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.service.QuizParticipationDomainService
import com.kominioai.global.exception.QuizParticipationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class QuizParticipationApplicationService(
    private val quizParticipationPersistencePort: QuizParticipationPersistencePort,
    private val surveyPersistencePort: SurveyPersistencePort,
    private val questionPersistencePort: QuestionPersistencePort,
    private val cacheQuizParticipationPort: CacheQuizParticipationPort,
    private val eventPublisherPort: EventPublisherPort,
    private val quizParticipationDomainService: QuizParticipationDomainService
) : StartQuizParticipationUseCase, SubmitQuizAnswerUseCase, SubmitQuizParticipationUseCase, GetQuizParticipationUseCase, ParticipateQuizUseCase {

    private val logger = LoggerFactory.getLogger(QuizParticipationApplicationService::class.java)

    override fun participate(command: ParticipateQuizCommand): Mono<Void> {
        return surveyPersistencePort.findById(command.surveyId)
            .flatMap { survey ->
                val validationResult = quizParticipationDomainService.validateParticipation(survey, command.participantInfo)

                if (!validationResult.isSuccess()) {
                    val errorMessage = (validationResult as ValidationResult.Failure).message
                    Mono.error<Void>(QuizParticipationException.ParticipationNotAllowed(errorMessage))
                } else {
                    val participation = QuizParticipation.create(
                        surveyId = command.surveyId,
                        participant = command.participantInfo
                    )

                    command.responses.forEach { response ->
                        val question = survey.getQuestions().find { it.id.value == response.questionId.value }
                            ?: throw QuizParticipationException.QuestionNotFound(response.questionId.value)

                        val answer = createAnswer(response, question)
                        participation.addAnswer(answer)
                    }

                    quizParticipationPersistencePort.save(participation)
                        .flatMap { savedParticipation ->
                            cacheQuizParticipationPort.cacheParticipation(savedParticipation)
                                .then(eventPublisherPort.publish(QuizParticipationEvent.ParticipationStarted(
                                    participationId = savedParticipation.id,
                                    surveyId = savedParticipation.surveyId,
                                    participantName = savedParticipation.participant.name ?: "",
                                    participantPhone = savedParticipation.participant.phone ?: "",
                                    startedAt = savedParticipation.startedAt
                                )))
                                .then()
                        }
                }
            }
            .doOnSuccess {
                logger.info("퀴즈 참여 성공: surveyId={}, participantName={}", command.surveyId.value, command.participantInfo.name)
            }
            .doOnError { error ->
                logger.error("퀴즈 참여 실패: surveyId={}, participantName={}, error={}",
                    command.surveyId.value, command.participantInfo.name, error.message)
            }
    }

    override fun startParticipation(command: StartQuizParticipationCommand): Mono<QuizParticipationResponse> {
        return surveyPersistencePort.findById(command.surveyId)
            .flatMap { survey ->
                val participantInfo = ParticipantInfo(
                    userId = command.userId?.value,
                    name = command.participantName,
                    phone = command.participantPhone,
                    authenticated = command.userId != null
                )

                val validationResult = quizParticipationDomainService.validateParticipation(survey, participantInfo)

                if (!validationResult.isSuccess()) {
                    val errorMessage = (validationResult as ValidationResult.Failure).message
                    Mono.error<QuizParticipationResponse>(QuizParticipationException.ParticipationNotAllowed(errorMessage))
                } else {
                    val participation = QuizParticipation.create(
                        surveyId = command.surveyId,
                        participant = participantInfo
                    )

                    quizParticipationPersistencePort.save(participation)
                        .flatMap { savedParticipation ->
                            cacheQuizParticipationPort.cacheParticipation(savedParticipation)
                                .then(eventPublisherPort.publish(QuizParticipationEvent.ParticipationStarted(
                                    participationId = savedParticipation.id,
                                    surveyId = savedParticipation.surveyId,
                                    participantName = savedParticipation.participant.name ?: "",
                                    participantPhone = savedParticipation.participant.phone ?: "",
                                    startedAt = savedParticipation.startedAt
                                )))
                                .thenReturn(QuizParticipationResponse(
                                    participationId = savedParticipation.id.value,
                                    surveyId = savedParticipation.surveyId.value,
                                    participantName = savedParticipation.participant.name ?: "",
                                    status = savedParticipation.getStatus().name,
                                    startedAt = savedParticipation.startedAt,
                                    submittedAt = savedParticipation.getSubmittedAt(),
                                    timeLimit = survey.timeLimit?.minutes,
                                    remainingTime = calculateRemainingTime(survey.timeLimit?.minutes, savedParticipation.startedAt)
                                ))
                        }
                }
            }
            .doOnSuccess { response ->
                logger.info("퀴즈 참여 시작 성공: participationId={}, surveyId={}, participantName={}",
                    response.participationId, response.surveyId, response.participantName)
            }
            .doOnError { error ->
                logger.error("퀴즈 참여 시작 실패: surveyId={}, participantName={}, error={}",
                    command.surveyId.value, command.participantName, error.message)
            }
    }

    override fun submitAnswer(command: SubmitQuizAnswerCommand): Mono<QuizParticipationResponse> {
        return quizParticipationPersistencePort.findById(command.participationId)
            .flatMap { participation ->
                surveyPersistencePort.findById(participation.surveyId)
                    .flatMap { survey ->
                        val question = survey.getQuestions().find { it.id.value == command.questionId }
                            ?: throw QuizParticipationException.QuestionNotFound(command.questionId)

                        val answer = createAnswer(command, question)
                        participation.addAnswer(answer)

                        quizParticipationPersistencePort.save(participation)
                            .flatMap { savedParticipation ->
                                cacheQuizParticipationPort.cacheParticipation(savedParticipation)
                                    .then(eventPublisherPort.publish(QuizParticipationEvent.AnswerSubmitted(
                                        participationId = savedParticipation.id,
                                        surveyId = savedParticipation.surveyId,
                                        questionId = command.questionId,
                                        submittedAt = answer.submittedAt
                                    )))
                                    .thenReturn(QuizParticipationResponse(
                                        participationId = savedParticipation.id.value,
                                        surveyId = savedParticipation.surveyId.value,
                                        participantName = savedParticipation.participant.name ?: "",
                                        status = savedParticipation.getStatus().name,
                                        startedAt = savedParticipation.startedAt,
                                        submittedAt = savedParticipation.getSubmittedAt(),
                                        timeLimit = survey.timeLimit?.minutes,
                                        remainingTime = calculateRemainingTime(survey.timeLimit?.minutes, savedParticipation.startedAt)
                                    ))
                            }
                    }
            }
            .doOnSuccess { response ->
                logger.info("퀴즈 답변 제출 성공: participationId={}, questionId={}",
                    response.participationId, command.questionId)
            }
            .doOnError { error ->
                logger.error("퀴즈 답변 제출 실패: participationId={}, questionId={}, error={}",
                    command.participationId.value, command.questionId, error.message)
            }
    }

    override fun submitParticipation(command: SubmitQuizParticipationCommand): Mono<QuizParticipationResponse> {
        return quizParticipationPersistencePort.findById(command.participationId)
            .flatMap { participation ->
                surveyPersistencePort.findById(participation.surveyId)
                    .flatMap { survey ->
                        val validationResult = participation.validateRequiredAnswers(survey.getQuestions())

                        if (!validationResult.isSuccess()) {
                            val errorMessage = (validationResult as ValidationResult.Failure).message
                            Mono.error<QuizParticipationResponse>(QuizParticipationException.RequiredAnswerMissing(errorMessage))
                        } else {
                            participation.submit()

                            quizParticipationPersistencePort.save(participation)
                                .flatMap { savedParticipation ->
                                    cacheQuizParticipationPort.cacheParticipation(savedParticipation)
                                        .then(eventPublisherPort.publish(QuizParticipationEvent.ParticipationSubmitted(
                                            participationId = savedParticipation.id,
                                            surveyId = savedParticipation.surveyId,
                                            participantName = savedParticipation.participant.name ?: "",
                                            submittedAt = savedParticipation.getSubmittedAt()!!
                                        )))
                                        .thenReturn(QuizParticipationResponse(
                                            participationId = savedParticipation.id.value,
                                            surveyId = savedParticipation.surveyId.value,
                                            participantName = savedParticipation.participant.name ?: "",
                                            status = savedParticipation.getStatus().name,
                                            startedAt = savedParticipation.startedAt,
                                            submittedAt = savedParticipation.getSubmittedAt(),
                                            timeLimit = survey.timeLimit?.minutes,
                                            remainingTime = calculateRemainingTime(survey.timeLimit?.minutes, savedParticipation.startedAt)
                                        ))
                                }
                        }
                    }
            }
            .doOnSuccess { response ->
                logger.info("퀴즈 참여 제출 성공: participationId={}", response.participationId)
            }
            .doOnError { error ->
                logger.error("퀴즈 참여 제출 실패: participationId={}, error={}",
                    command.participationId.value, error.message)
            }
    }

    override fun getParticipationDetails(query: GetQuizParticipationQuery): Mono<QuizParticipationDetailResponse> {
        return quizParticipationPersistencePort.findById(query.participationId)
            .flatMap { participation ->
                surveyPersistencePort.findById(participation.surveyId)
                    .map { survey ->
                        QuizParticipationDetailResponse(
                            participationId = participation.id.value,
                            surveyId = participation.surveyId.value,
                            surveyTitle = survey.getTitle().value,
                            participantName = participation.participant.name ?: "",
                            participantPhone = participation.participant.phone ?: "",
                            status = participation.getStatus().name,
                            startedAt = participation.startedAt,
                            submittedAt = participation.getSubmittedAt(),
                            timeLimit = survey.timeLimit?.minutes,
                            remainingTime = calculateRemainingTime(survey.timeLimit?.minutes, participation.startedAt),
                            answers = participation.getAnswers().map { answer ->
                                QuizAnswerResponse(
                                    questionId = answer.questionId.value,
                                    answerType = getAnswerType(answer),
                                    answerContent = getAnswerContent(answer)
                                )
                            }
                        )
                    }
            }
            .doOnSuccess { response ->
                logger.info("퀴즈 참여 상세 조회 성공: participationId={}", response.participationId)
            }
            .doOnError { error ->
                logger.error("퀴즈 참여 상세 조회 실패: participationId={}, error={}",
                    query.participationId.value, error.message)
            }
    }

    private fun createAnswer(response: QuestionResponse, question: Question): QuizAnswer {
        return when (question.type) {
            QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                QuizAnswer.createSingleChoice(
                    questionId = response.questionId,
                    selectedOptionId = QuestionOptionId.fromString(response.answer as String)
                )
            }
            QuestionType.MULTIPLE_CHOICE -> {
                QuizAnswer.createMultipleChoice(
                    questionId = response.questionId,
                    selectedOptionIds = (response.answer as List<String>).map { QuestionOptionId.fromString(it) }
                )
            }
            QuestionType.QUIZ_SHORT_ANSWER, QuestionType.SHORT_ANSWER -> {
                QuizAnswer.createShortText(
                    questionId = response.questionId,
                    text = response.answer as String
                )
            }
            QuestionType.QUIZ_ESSAY, QuestionType.ESSAY -> {
                QuizAnswer.createLongText(
                    questionId = response.questionId,
                    text = response.answer as String
                )
            }
            else -> throw QuizParticipationException.InvalidQuestionType("지원하지 않는 질문 타입입니다: ${question.type}")
        }
    }

    private fun createAnswer(command: SubmitQuizAnswerCommand, question: Question): QuizAnswer {
        return when (question.type) {
            QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                QuizAnswer.createSingleChoice(
                    questionId = QuestionId.fromString(command.questionId),
                    selectedOptionId = QuestionOptionId.fromString(command.answer as String)
                )
            }
            QuestionType.MULTIPLE_CHOICE -> {
                QuizAnswer.createMultipleChoice(
                    questionId = QuestionId.fromString(command.questionId),
                    selectedOptionIds = (command.answer as List<String>).map { QuestionOptionId.fromString(it) }
                )
            }
            QuestionType.QUIZ_SHORT_ANSWER, QuestionType.SHORT_ANSWER -> {
                QuizAnswer.createShortText(
                    questionId = QuestionId.fromString(command.questionId),
                    text = command.answer as String
                )
            }
            QuestionType.QUIZ_ESSAY, QuestionType.ESSAY -> {
                QuizAnswer.createLongText(
                    questionId = QuestionId.fromString(command.questionId),
                    text = command.answer as String
                )
            }
            else -> throw QuizParticipationException.InvalidQuestionType("지원하지 않는 질문 타입입니다: ${question.type}")
        }
    }

    private fun getAnswerType(answer: QuizAnswer): String {
        return when (answer) {
            is QuizAnswer.SingleChoice -> "SINGLE_CHOICE"
            is QuizAnswer.MultipleChoice -> "MULTIPLE_CHOICE"
            is QuizAnswer.ShortText -> "SHORT_TEXT"
            is QuizAnswer.LongText -> "LONG_TEXT"
        }
    }

    private fun getAnswerContent(answer: QuizAnswer): Any {
        return when (answer) {
            is QuizAnswer.SingleChoice -> answer.selectedOptionId.value
            is QuizAnswer.MultipleChoice -> answer.selectedOptionIds.map { it.value }
            is QuizAnswer.ShortText -> answer.text
            is QuizAnswer.LongText -> answer.text
        }
    }

    private fun calculateRemainingTime(timeLimitMinutes: Int?, startedAt: LocalDateTime): Long {
        if (timeLimitMinutes == null) return -1L

        val endTime = startedAt.plusMinutes(timeLimitMinutes.toLong())
        val remainingSeconds = java.time.Duration.between(LocalDateTime.now(), endTime).seconds

        return if (remainingSeconds > 0) remainingSeconds else 0L
    }
}