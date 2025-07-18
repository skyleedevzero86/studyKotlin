package com.kominioai.domain.survey.adapter.`in`.web

import com.kominioai.domain.survey.adapter.`in`.web.dto.*
import com.kominioai.domain.survey.application.dto.*
import com.kominioai.domain.survey.application.port.`in`.*
import com.kominioai.domain.survey.application.query.QuizDetailQuery
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.global.common.Result
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/quiz")
class QuizController(
    private val getQuizDetailUseCase: GetQuizDetailUseCase,
    private val participateQuizUseCase: ParticipateQuizUseCase
) {

    private val logger = LoggerFactory.getLogger(QuizController::class.java)

    @GetMapping("/{surveyId}")
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

    @PostMapping("/{surveyId}/participate")
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
}