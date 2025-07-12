package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.input.query.GetSurveyResponsesQuery
import com.kominioai.domain.survey.application.service.SurveyApplicationService
import com.kominioai.domain.survey.application.service.SurveyQueryService
import com.kominioai.domain.survey.application.service.SurveyResponseValidationService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.presentation.rest.dto.request.ValidatedSubmitResponseRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.ValidatedAnswerSubmission
import com.kominioai.domain.survey.presentation.rest.dto.response.ResponseSubmissionResult
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import com.kominioai.global.util.StructuredLogging
import com.kominioai.global.service.BusinessMetricsService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.InetSocketAddress

@RestController
@RequestMapping("/api/v2/responses")
class ValidatedSurveyResponseController(
    private val surveyApplicationService: SurveyApplicationService,
    private val surveyQueryService: SurveyQueryService,
    private val surveyResponseValidationService: SurveyResponseValidationService,
    private val businessMetricsService: BusinessMetricsService
) {
    
    private val logger = LoggerFactory.getLogger(ValidatedSurveyResponseController::class.java)
    
    @PostMapping
    fun submitResponse(
        @Valid @RequestBody request: ValidatedSubmitResponseRequest,
        @AuthenticationPrincipal user: UserDetails?,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<ResponseSubmissionResult>> {
        val startTime = System.currentTimeMillis()
        val requestId = StructuredLogging.generateRequestId()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Survey response submission started",
            "operation" to "SUBMIT_SURVEY_RESPONSE",
            "requestId" to requestId,
            "surveyId" to request.surveyId,
            "respondentId" to user?.username,
            "answersCount" to request.answers.size,
            "ipAddress" to extractClientIpAddress(exchange)
        )
        
        // IP 주소 추출
        val ipAddress = extractClientIpAddress(exchange)
        
        // 도메인 검증 서비스를 통한 추가 검증
        return surveyResponseValidationService.validateSurveyResponseSubmission(
            surveyId = SurveyId.from(request.surveyId),
            questionIds = request.answers.map { it.questionId },
            respondentId = user?.username,
            ipAddress = ipAddress
        )
        .flatMap {
            // 답변 내용의 비즈니스 로직 검증
            validateAnswerContents(request.answers)
            
            // 도메인 모델로 변환
            val answers = request.answers.map { validatedAnswer ->
                com.kominioai.domain.survey.domain.model.domain.Answer.create(
                    responseId = "",
                    questionId = QuestionId.from(validatedAnswer.questionId),
                    questionType = determineQuestionType(validatedAnswer),
                    textAnswer = validatedAnswer.answerText,
                    selectedOptions = emptyList() // 실제 구현에서는 옵션 정보를 로드해야 함
                )
            }
            
            val command = SubmitResponseCommand(
                surveyId = SurveyId.from(request.surveyId),
                respondentId = user?.username,
                answers = answers,
                ipAddress = ipAddress
            )
            
            surveyApplicationService.submitResponse(command)
        }
        .map { responseId ->
            val duration = System.currentTimeMillis() - startTime
            
            // 비즈니스 메트릭 기록
            businessMetricsService.recordSurveyResponse(
                surveyId = request.surveyId,
                responseId = responseId.value,
                userId = user?.username ?: "anonymous",
                responseTimeMs = duration
            )
            
            StructuredLogging.logInfo(
                logger = logger,
                message = "Survey response submission completed successfully",
                "operation" to "SUBMIT_SURVEY_RESPONSE",
                "requestId" to requestId,
                "surveyId" to request.surveyId,
                "responseId" to responseId.value,
                "duration" to duration,
                "respondentId" to user?.username
            )
            
            ResponseSubmissionResult(responseId.value)
        }
        .map { result ->
            ResponseEntity.status(HttpStatus.CREATED).body(result)
        }
        .onErrorResume { error ->
            val duration = System.currentTimeMillis() - startTime
            
            StructuredLogging.logError(
                logger = logger,
                message = "Survey response submission failed",
                throwable = error,
                "operation" to "SUBMIT_SURVEY_RESPONSE",
                "requestId" to requestId,
                "surveyId" to request.surveyId,
                "duration" to duration,
                "respondentId" to user?.username,
                "errorType" to error.javaClass.simpleName
            )
            
            when (error) {
                is com.kominioai.global.exception.SurveyValidationException -> {
                    Mono.just(ResponseEntity.badRequest().body(ResponseSubmissionResult("")))
                }
                is com.kominioai.global.exception.SurveyNotFoundException -> {
                    Mono.just(ResponseEntity.notFound().build())
                }
                else -> {
                    Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseSubmissionResult("")))
                }
            }
        }
    }
    
    @GetMapping("/survey/{surveyId}")
    fun getSurveyResponses(@PathVariable surveyId: String): Mono<ResponseEntity<Flux<SurveyResponseDto>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Survey responses retrieval started",
            "operation" to "GET_SURVEY_RESPONSES",
            "surveyId" to surveyId
        )
        
        // UUID 형식 검증
        if (!surveyId.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
            StructuredLogging.logWarn(
                logger = logger,
                message = "Invalid survey ID format",
                "operation" to "GET_SURVEY_RESPONSES",
                "surveyId" to surveyId,
                "warningType" to "INVALID_SURVEY_ID_FORMAT"
            )
            return Mono.just(ResponseEntity.badRequest().build())
        }
        
        val query = GetSurveyResponsesQuery(SurveyId.from(surveyId))
        val responses = surveyQueryService.getSurveyResponses(query)
        
        return responses.collectList()
            .map { responseList ->
                val duration = System.currentTimeMillis() - startTime
                
                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Survey responses retrieved successfully",
                    "operation" to "GET_SURVEY_RESPONSES",
                    "surveyId" to surveyId,
                    "duration" to duration,
                    "responseCount" to responseList.size
                )
                
                ResponseEntity.ok(Flux.fromIterable(responseList))
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                
                StructuredLogging.logError(
                    logger = logger,
                    message = "Survey responses retrieval failed",
                    throwable = error,
                    "operation" to "GET_SURVEY_RESPONSES",
                    "surveyId" to surveyId,
                    "duration" to duration
                )
                
                Mono.error(error)
            }
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private fun extractClientIpAddress(exchange: ServerWebExchange): String? {
        return exchange.request.headers.getFirst("X-Forwarded-For")
            ?: exchange.request.headers.getFirst("X-Real-IP")
            ?: (exchange.request.remoteAddress as? InetSocketAddress)?.address?.hostAddress
    }
    
    /**
     * 답변 내용의 비즈니스 로직 검증
     */
    private fun validateAnswerContents(answers: List<ValidatedAnswerSubmission>) {
        answers.forEach { answer ->
            // 텍스트 답변과 선택된 옵션 중 하나는 반드시 있어야 함
            if (answer.answerText.isNullOrBlank() && answer.selectedOptionIds.isEmpty()) {
                StructuredLogging.logError(
                    logger = logger,
                    message = "Answer validation failed: missing text or selected options",
                    throwable = IllegalArgumentException("Missing answer content"),
                    "operation" to "VALIDATE_ANSWER_CONTENTS",
                    "questionId" to answer.questionId,
                    "errorType" to "MISSING_ANSWER_CONTENT"
                )
                throw com.kominioai.global.exception.SurveyValidationException(
                    "질문 ${answer.questionId}: 텍스트 답변이나 선택된 옵션 중 하나는 반드시 입력해야 합니다"
                )
            }
            
            // 텍스트 답변이 있는 경우 길이 검증
            if (!answer.answerText.isNullOrBlank() && answer.answerText.length > 2000) {
                StructuredLogging.logError(
                    logger = logger,
                    message = "Answer validation failed: text too long",
                    throwable = IllegalArgumentException("Text too long"),
                    "operation" to "VALIDATE_ANSWER_CONTENTS",
                    "questionId" to answer.questionId,
                    "textLength" to answer.answerText.length,
                    "maxLength" to 2000,
                    "errorType" to "TEXT_TOO_LONG"
                )
                throw com.kominioai.global.exception.SurveyValidationException(
                    "질문 ${answer.questionId}: 답변 텍스트는 2000자를 초과할 수 없습니다"
                )
            }
            
            // 선택된 옵션이 있는 경우 개수 검증
            if (answer.selectedOptionIds.size > 10) {
                StructuredLogging.logError(
                    logger = logger,
                    message = "Answer validation failed: too many selected options",
                    throwable = IllegalArgumentException("Too many selected options"),
                    "operation" to "VALIDATE_ANSWER_CONTENTS",
                    "questionId" to answer.questionId,
                    "selectedOptionsCount" to answer.selectedOptionIds.size,
                    "maxOptions" to 10,
                    "errorType" to "TOO_MANY_OPTIONS"
                )
                throw com.kominioai.global.exception.SurveyValidationException(
                    "질문 ${answer.questionId}: 선택 가능한 옵션은 최대 10개까지입니다"
                )
            }
        }
    }
    
    /**
     * 답변 유형에 따른 질문 타입 결정
     * 실제 구현에서는 설문지에서 질문 정보를 조회해야 함
     */
    private fun determineQuestionType(answer: ValidatedAnswerSubmission): com.kominioai.domain.survey.domain.valueobject.QuestionType {
        return when {
            !answer.answerText.isNullOrBlank() && answer.selectedOptionIds.isEmpty() -> {
                com.kominioai.domain.survey.domain.valueobject.QuestionType.TEXT
            }
            answer.answerText.isNullOrBlank() && answer.selectedOptionIds.size == 1 -> {
                com.kominioai.domain.survey.domain.valueobject.QuestionType.SINGLE_CHOICE
            }
            answer.answerText.isNullOrBlank() && answer.selectedOptionIds.size > 1 -> {
                com.kominioai.domain.survey.domain.valueobject.QuestionType.MULTIPLE_CHOICE
            }
            else -> {
                com.kominioai.domain.survey.domain.valueobject.QuestionType.TEXT
            }
        }
    }
} 