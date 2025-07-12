package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.application.port.input.SurveyUseCase
import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.request.AddQuestionRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.CreateSurveyRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.PublishSurveyRequest
import com.kominioai.domain.survey.presentation.rest.dto.response.AddQuestionResponse
import com.kominioai.domain.survey.presentation.rest.dto.response.CreateSurveyResponse
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/surveys")
class SurveyController(
    private val surveyApplicationService: SurveyUseCase,
    private val businessMetricsService: BusinessMetricsService
) {

    private val logger = LoggerFactory.getLogger(SurveyController::class.java)

    @PostMapping
    fun createSurvey(@Valid @RequestBody request: CreateSurveyRequest): Mono<ResponseEntity<CreateSurveyResponse>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "Survey creation started",
            "operation" to "CREATE_SURVEY",
            "title" to request.title,
            "createdBy" to request.createdBy,
            "allowAnonymous" to request.allowAnonymous,
            "allowMultipleResponses" to request.allowMultipleResponses,
            "requireLogin" to request.requireLogin,
            "collectIpAddress" to request.collectIpAddress
        )

        val command = CreateSurveyCommand(
            title = request.title,
            description = request.description,
            createdBy = UserId.from(request.createdBy),
            settings = com.kominioai.domain.survey.domain.model.SurveySettings(
                allowAnonymous = request.allowAnonymous,
                allowMultipleResponses = request.allowMultipleResponses,
                requireLogin = request.requireLogin,
                collectIpAddress = request.collectIpAddress
            )
        )

        return surveyApplicationService.createSurvey(command)
            .map { surveyId ->
                val duration = System.currentTimeMillis() - startTime

                businessMetricsService.recordSurveyCreation(
                    surveyId = surveyId.value,
                    questionCount = 0,
                    userId = request.createdBy
                )

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Survey created successfully",
                    "operation" to "CREATE_SURVEY",
                    "surveyId" to surveyId.value,
                    "duration" to duration
                )

                ResponseEntity.status(HttpStatus.CREATED).body(CreateSurveyResponse(surveyId.value))
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "Survey creation failed",
                    throwable = error,
                    "operation" to "CREATE_SURVEY",
                    "duration" to duration,
                    "title" to request.title,
                    "createdBy" to request.createdBy
                )

                Mono.error<ResponseEntity<CreateSurveyResponse>>(error)
            }
    }

    @PostMapping("/{surveyId}/questions")
    fun addQuestion(
        @PathVariable surveyId: String,
        @Valid @RequestBody request: AddQuestionRequest
    ): Mono<ResponseEntity<AddQuestionResponse>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "Question addition started",
            "operation" to "ADD_QUESTION",
            "surveyId" to surveyId,
            "questionText" to request.text,
            "questionType" to request.type,
            "questionOrder" to request.order,
            "questionRequired" to request.required,
            "optionsCount" to request.options.size
        )

        val command = AddQuestionCommand(
            surveyId = SurveyId.from(surveyId),
            order = request.order,
            text = request.text,
            description = request.description,
            type = request.type,
            required = request.required,
            options = request.options
        )

        return surveyApplicationService.addQuestion(command)
            .map { questionId ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Question added successfully",
                    "operation" to "ADD_QUESTION",
                    "surveyId" to surveyId,
                    "questionId" to questionId.value,
                    "duration" to duration
                )

                ResponseEntity.status(HttpStatus.CREATED).body(AddQuestionResponse(questionId.value))
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "Question addition failed",
                    throwable = error,
                    "operation" to "ADD_QUESTION",
                    "surveyId" to surveyId,
                    "duration" to duration
                )

                Mono.error<ResponseEntity<AddQuestionResponse>>(error)
            }
    }

    @PostMapping("/{surveyId}/publish")
    fun publishSurvey(
        @PathVariable surveyId: String,
        @Valid @RequestBody request: PublishSurveyRequest
    ): Mono<ResponseEntity<Void>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "Survey publication started",
            "operation" to "PUBLISH_SURVEY",
            "surveyId" to surveyId,
            "userId" to request.userId
        )

        val command = PublishSurveyCommand(
            surveyId = SurveyId.from(surveyId),
            userId = UserId.from(request.userId)
        )

        return surveyApplicationService.publishSurvey(command)
            .map {
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Survey published successfully",
                    "operation" to "PUBLISH_SURVEY",
                    "surveyId" to surveyId,
                    "userId" to request.userId,
                    "duration" to duration
                )

                ResponseEntity.ok().build<Void>()
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "Survey publication failed",
                    throwable = error,
                    "operation" to "PUBLISH_SURVEY",
                    "surveyId" to surveyId,
                    "userId" to request.userId,
                    "duration" to duration
                )

                Mono.error<ResponseEntity<Void>>(error)
            }
    }

    @GetMapping("/{surveyId}")
    fun getSurvey(@PathVariable surveyId: String): Mono<ResponseEntity<SurveyDto>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "Survey retrieval started",
            "operation" to "GET_SURVEY",
            "surveyId" to surveyId
        )

        return surveyApplicationService.getSurvey(SurveyId.from(surveyId))
            .map { surveyDto ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Survey retrieved successfully",
                    "operation" to "GET_SURVEY",
                    "surveyId" to surveyId,
                    "duration" to duration
                )

                ResponseEntity.ok(surveyDto)
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "Survey retrieval failed",
                    throwable = error,
                    "operation" to "GET_SURVEY",
                    "surveyId" to surveyId,
                    "duration" to duration
                )

                Mono.error<ResponseEntity<SurveyDto>>(error)
            }
    }

    @GetMapping("/status/{status}")
    fun getSurveysByStatus(
        @PathVariable status: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "created_at,desc") sort: String
    ): Mono<ResponseEntity<Page<SurveyDto>>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "Surveys by status retrieval started",
            "operation" to "GET_SURVEYS_BY_STATUS",
            "status" to status,
            "page" to page,
            "size" to size,
            "sort" to sort
        )

        val surveyStatus = try {
            SurveyStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            return Mono.just(ResponseEntity.badRequest().build())
        }

        val pageable = org.springframework.data.domain.PageRequest.of(page, size, parseSort(sort))

        return surveyApplicationService.getSurveysByStatus(surveyStatus, pageable)
            .map { surveysPage ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Surveys by status retrieved successfully",
                    "operation" to "GET_SURVEYS_BY_STATUS",
                    "status" to status,
                    "duration" to duration,
                    "totalElements" to surveysPage.totalElements,
                    "totalPages" to surveysPage.totalPages,
                    "currentPage" to surveysPage.number,
                    "pageSize" to surveysPage.size,
                    "contentSize" to surveysPage.content.size
                )

                ResponseEntity.ok(surveysPage)
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "Surveys by status retrieval failed",
                    throwable = error,
                    "operation" to "GET_SURVEYS_BY_STATUS",
                    "status" to status,
                    "duration" to duration
                )

                Mono.error<ResponseEntity<Page<SurveyDto>>>(error)
            }
    }

    @GetMapping("/user/{userId}")
    fun getSurveysByUser(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "created_at,desc") sort: String
    ): Mono<ResponseEntity<Page<SurveyDto>>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "Surveys by user retrieval started",
            "operation" to "GET_SURVEYS_BY_USER",
            "userId" to userId,
            "page" to page,
            "size" to size,
            "sort" to sort
        )

        val pageable = org.springframework.data.domain.PageRequest.of(page, size, parseSort(sort))

        return surveyApplicationService.getSurveysByUser(UserId.from(userId), pageable)
            .map { surveysPage ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Surveys by user retrieved successfully",
                    "operation" to "GET_SURVEYS_BY_USER",
                    "userId" to userId,
                    "duration" to duration,
                    "totalElements" to surveysPage.totalElements,
                    "totalPages" to surveysPage.totalPages,
                    "currentPage" to surveysPage.number,
                    "pageSize" to surveysPage.size,
                    "contentSize" to surveysPage.content.size
                )

                ResponseEntity.ok(surveysPage)
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "Surveys by user retrieval failed",
                    throwable = error,
                    "operation" to "GET_SURVEYS_BY_USER",
                    "userId" to userId,
                    "duration" to duration
                )

                Mono.error<ResponseEntity<Page<SurveyDto>>>(error)
            }
    }

    @GetMapping("/published")
    fun getPublishedSurveys(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "created_at,desc") sort: String
    ): Mono<ResponseEntity<Page<SurveyDto>>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "Published surveys retrieval started",
            "operation" to "GET_PUBLISHED_SURVEYS",
            "page" to page,
            "size" to size,
            "sort" to sort
        )

        val pageable = org.springframework.data.domain.PageRequest.of(page, size, parseSort(sort))

        return surveyApplicationService.getPublishedSurveys(pageable)
            .map { surveysPage ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Published surveys retrieved successfully",
                    "operation" to "GET_PUBLISHED_SURVEYS",
                    "duration" to duration,
                    "totalElements" to surveysPage.totalElements,
                    "totalPages" to surveysPage.totalPages,
                    "currentPage" to surveysPage.number,
                    "pageSize" to surveysPage.size,
                    "contentSize" to surveysPage.content.size
                )

                ResponseEntity.ok(surveysPage)
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "Published surveys retrieval failed",
                    throwable = error,
                    "operation" to "GET_PUBLISHED_SURVEYS",
                    "duration" to duration
                )

                Mono.error<ResponseEntity<Page<SurveyDto>>>(error)
            }
    }

    @GetMapping
    fun getAllSurveys(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "created_at,desc") sort: String
    ): Mono<ResponseEntity<Page<SurveyDto>>> {
        val startTime = System.currentTimeMillis()

        StructuredLogging.logInfo(
            logger = logger,
            message = "All surveys retrieval started",
            "operation" to "GET_ALL_SURVEYS",
            "page" to page,
            "size" to size,
            "sort" to sort
        )

        val pageable = org.springframework.data.domain.PageRequest.of(page, size, parseSort(sort))

        return surveyApplicationService.getAllSurveys(pageable)
            .map { surveysPage ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logInfo(
                    logger = logger,
                    message = "All surveys retrieved successfully",
                    "operation" to "GET_ALL_SURVEYS",
                    "duration" to duration,
                    "totalElements" to surveysPage.totalElements,
                    "totalPages" to surveysPage.totalPages,
                    "currentPage" to surveysPage.number,
                    "pageSize" to surveysPage.size,
                    "contentSize" to surveysPage.content.size
                )

                ResponseEntity.ok(surveysPage)
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logError(
                    logger = logger,
                    message = "All surveys retrieval failed",
                    throwable = error,
                    "operation" to "GET_ALL_SURVEYS",
                    "duration" to duration
                )

                Mono.error<ResponseEntity<Page<SurveyDto>>>(error)
            }
    }

    private fun parseSort(sort: String): org.springframework.data.domain.Sort {
        return try {
            val parts = sort.split(",")
            val property = parts[0]
            val direction = if (parts.size > 1 && parts[1].equals("desc", ignoreCase = true)) {
                org.springframework.data.domain.Sort.Direction.DESC
            } else {
                org.springframework.data.domain.Sort.Direction.ASC
            }
            org.springframework.data.domain.Sort.by(direction, property)
        } catch (e: Exception) {
            StructuredLogging.logWarn(
                logger = logger,
                message = "Invalid sort parameter, using default sort",
                "invalidSort" to sort,
                "errorMessage" to e.message,
                "warningType" to "INVALID_SORT_PARAMETER"
            )
            org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "created_at")
        }
    }
}