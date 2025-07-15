package com.kominioai.domain.survey.adapter.`in`.web

import com.kominioai.domain.survey.adapter.`in`.web.dto.*
import com.kominioai.domain.survey.application.dto.*
import com.kominioai.domain.survey.application.port.`in`.*
import com.kominioai.domain.survey.application.query.QuizDetailQuery
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.global.common.Result
import com.kominioai.global.exception.QuizParticipationException
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1")
class QuizController(
    private val getQuizDetailUseCase: GetQuizDetailUseCase,
    private val participateQuizUseCase: ParticipateQuizUseCase,
    private val startQuizParticipationUseCase: StartQuizParticipationUseCase,
    private val submitQuizAnswerUseCase: SubmitQuizAnswerUseCase,
    private val submitQuizParticipationUseCase: SubmitQuizParticipationUseCase,
    private val getQuizParticipationUseCase: GetQuizParticipationUseCase
) {

    private val logger = LoggerFactory.getLogger(QuizController::class.java)

    @GetMapping("/quiz/{surveyId}")
    fun getQuizDetail(@PathVariable surveyId: String): Mono<ResponseEntity<Result<QuizDetailResponse>>> {
        val query = QuizDetailQuery(SurveyId.fromString(surveyId))

        return getQuizDetailUseCase.getQuizDetail(query)
            .map { quizDetail ->
                ResponseEntity.ok(Result.success(quizDetail))
            }
            .onErrorResume { error ->
                logger.error("퀴즈 상세 조회 실패: surveyId={}, error={}", surveyId, error.message)
                Mono.just(ResponseEntity.badRequest().body(Result.failure<QuizDetailResponse>(error)))
            }
    }

    @PostMapping("/quiz/{surveyId}/participate")
    fun participateQuiz(
        @PathVariable surveyId: String,
        @RequestBody request: ParticipateQuizRequest
    ): Mono<ResponseEntity<Result<Unit>>> {

        val command = ParticipateQuizCommand(
            surveyId = SurveyId.fromString(surveyId),
            participantInfo = ParticipantInfo(
                userId = request.userId,
                name = request.name,
                phone = request.phone,
                authenticated = request.authenticated
            ),
            responses = request.responses.map { response ->
                QuestionResponse(
                    questionId = QuestionId.fromString(response.questionId),
                    answer = response.answer
                )
            }
        )

        return participateQuizUseCase.participate(command)
            .map {
                ResponseEntity.ok(Result.success(Unit))
            }
            .onErrorResume { error ->
                logger.error("퀴즈 참여 실패: surveyId={}, error={}", surveyId, error.message)
                Mono.just(ResponseEntity.badRequest().body(Result.failure<Unit>(error)))
            }
    }

    @PostMapping("/quiz-participations/start")
    fun startParticipation(
        @Valid @RequestBody request: StartQuizParticipationRequest
    ): Mono<ResponseEntity<Result<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse>>> {
        val command = StartQuizParticipationCommand(
            surveyId = SurveyId.fromString(request.surveyId),
            participantName = request.participantName,
            participantPhone = request.participantPhone,
            userId = request.userId?.let { UserId.fromString(it) }
        )

        return startQuizParticipationUseCase.startParticipation(command)
            .map { participation ->
                ResponseEntity.ok(Result.success(com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse.from(participation)))
            }
            .onErrorResume { error ->
                logger.error("퀴즈 참여 시작 실패: surveyId={}, error={}", request.surveyId, error.message)
                Mono.just(ResponseEntity.badRequest().body(Result.failure<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse>(error)))
            }
    }

    @PostMapping("/quiz-participations/{participationId}/answers")
    fun submitAnswer(
        @PathVariable participationId: String,
        @Valid @RequestBody request: SubmitQuizAnswerRequest
    ): Mono<ResponseEntity<Result<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse>>> {
        val command = SubmitQuizAnswerCommand(
            participationId = ParticipationId.fromString(participationId),
            questionId = request.questionId,
            answer = request.answer
        )

        return submitQuizAnswerUseCase.submitAnswer(command)
            .map { participation ->
                ResponseEntity.ok(Result.success(com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse.from(participation)))
            }
            .onErrorResume { error ->
                logger.error("퀴즈 답변 제출 실패: participationId={}, questionId={}, error={}",
                    participationId, request.questionId, error.message)
                Mono.just(ResponseEntity.badRequest().body(Result.failure<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse>(error)))
            }
    }

    @PostMapping("/quiz-participations/{participationId}/submit")
    fun submitParticipation(
        @PathVariable participationId: String
    ): Mono<ResponseEntity<Result<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse>>> {
        val command = SubmitQuizParticipationCommand(
            participationId = ParticipationId.fromString(participationId)
        )

        return submitQuizParticipationUseCase.submitParticipation(command)
            .map { participation ->
                ResponseEntity.ok(Result.success(com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse.from(participation)))
            }
            .onErrorResume { error ->
                logger.error("퀴즈 참여 제출 실패: participationId={}, error={}", participationId, error.message)
                Mono.just(ResponseEntity.badRequest().body(Result.failure<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationResponse>(error)))
            }
    }

    @GetMapping("/quiz-participations/{participationId}")
    fun getParticipation(
        @PathVariable participationId: String
    ): Mono<ResponseEntity<Result<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationDetailResponse>>> {
        val query = GetQuizParticipationQuery(
            participationId = ParticipationId.fromString(participationId)
        )

        return getQuizParticipationUseCase.getParticipationDetails(query)
            .map { participation ->
                ResponseEntity.ok(Result.success(com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationDetailResponse.from(participation)))
            }
            .onErrorResume { error ->
                logger.error("퀴즈 참여 상세 조회 실패: participationId={}, error={}", participationId, error.message)
                Mono.just(ResponseEntity.badRequest().body(Result.failure<com.kominioai.domain.survey.adapter.`in`.web.dto.QuizParticipationDetailResponse>(error)))
            }
    }

    @GetMapping("/quiz-participations/survey/{surveyId}/participants")
    fun getParticipantsBySurvey(
        @PathVariable surveyId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): Mono<ResponseEntity<Result<QuizParticipantListResponse>>> {
        return Mono.just(ResponseEntity.ok(Result.success(QuizParticipantListResponse.empty())))
    }
}