package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Survey as SurveyEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Question as QuestionEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.QuestionOption as QuestionOptionEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.SurveyR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionOptionR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.dto.SurveyJoinResult
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.dto.toSurveyWithQuestions
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2dbcSurveyRepositoryAdapter(
    private val surveyRepository: SurveyR2dbcRepository,
    private val questionRepository: QuestionR2dbcRepository,
    private val questionOptionRepository: QuestionOptionR2dbcRepository,
    private val surveyDataLoader: SurveyDataLoader,
    private val performanceMetrics: SurveyPerformanceMetrics,
    private val surveyCacheService: com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
) : SurveyRepository {

    private val logger = LoggerFactory.getLogger(R2dbcSurveyRepositoryAdapter::class.java)

    override fun save(survey: Survey): Mono<Survey> {
        logger.debug("Saving survey: ${survey.id.value}")

        val startTime = System.currentTimeMillis()

        val surveyEntity = SurveyEntity.from(survey)

        return surveyRepository.save(surveyEntity)
            .flatMap { savedSurveyEntity ->
                val savedSurvey = savedSurveyEntity.toDomain()

                // 질문과 옵션들을 별도로 저장
                saveQuestionsAndOptions(survey, savedSurvey.id.value)
                    .thenReturn(savedSurvey)
            }
            .doOnSuccess { savedSurvey ->
                val duration = System.currentTimeMillis() - startTime
                performanceMetrics.recordQueryTime("save", duration)

                // 기존 캐시 무효화
                surveyDataLoader.invalidateCache(savedSurvey.id.value)

                // Redis 캐시 무효화
                surveyCacheService.invalidateSurveyCache(savedSurvey.id)

                // 게시된 설문조사 목록 캐시도 무효화
                surveyCacheService.invalidatePublishedSurveysCache()

                logger.debug("Survey saved and all caches invalidated for surveyId: ${savedSurvey.id.value}")
            }
    }

    override fun findById(id: SurveyId): Mono<Survey> {
        logger.debug("Finding survey by ID: ${id.value}")

        val startTime = System.currentTimeMillis()

        return surveyCacheService.getSurveyById(id)
            .flatMap { cachedSurvey ->
                if (cachedSurvey != null) {
                    Mono.just(cachedSurvey)
                } else {
                    // 캐시 미스 시 데이터베이스에서 조회
                    surveyRepository.findById(id.value)
                        .map { it.toDomain() }
                        .flatMap { survey ->
                            // 조회된 데이터를 캐시에 저장
                            surveyCacheService.cacheSurvey(survey)
                                .thenReturn(survey)
                        }
                }
            }
            .switchIfEmpty(Mono.error(com.kominioai.global.exception.SurveyNotFoundException("Survey not found with id: ${id.value}")))
            .doOnSuccess { survey ->
                val duration = System.currentTimeMillis() - startTime
                performanceMetrics.recordQueryTime("findById", duration)
                logger.debug("Survey found by ID in ${duration}ms: ${id.value}")
            }
            .onErrorResume { error ->
                logger.error("Error finding survey by ID: ${id.value}, error: ${error.message}", error)
                Mono.error(error)
            }
    }

    override fun findAll(): Flux<Survey> {
        return surveyRepository.findAll()
            .map { it.toDomain() }
    }

    override fun findByStatus(status: SurveyStatus): Flux<Survey> {
        return surveyRepository.findByStatus(status)
            .map { it.toDomain() }
    }

    override fun findByCreatedBy(userId: UserId): Flux<Survey> {
        return surveyRepository.findByCreatedBy(userId.value)
            .map { it.toDomain() }
    }

    override fun findPublishedSurveys(): Flux<Survey> {
        logger.debug("Finding published surveys")

        val startTime = System.currentTimeMillis()

        return surveyCacheService.getPublishedSurveys()
            .flatMapMany { cachedSurveys ->
                if (cachedSurveys != null) {
                    // 캐시 히트 시 캐시된 데이터 반환
                    Flux.fromIterable(cachedSurveys)
                } else {
                    // 캐시 미스 시 데이터베이스에서 조회
                    surveyRepository.findPublishedSurveys()
                        .map { it.toDomain() }
                        .collectList()
                        .flatMapMany { surveys ->
                            // 조회된 데이터를 캐시에 저장
                            surveyCacheService.cachePublishedSurveys(surveys)
                                .thenMany(Flux.fromIterable(surveys))
                        }
                }
            }
            .doOnComplete {
                val duration = System.currentTimeMillis() - startTime
                performanceMetrics.recordQueryTime("findPublishedSurveys", duration)
                logger.debug("Published surveys found in ${duration}ms")
            }
            .onErrorResume { error ->
                logger.error("Error finding published surveys, error: ${error.message}", error)
                Flux.error(error)
            }
    }

    override fun delete(id: SurveyId): Mono<Void> {
        return surveyRepository.deleteById(id.value)
            .doOnSuccess {
                // 기존 캐시 무효화
                surveyDataLoader.invalidateCache(id.value)

                // Redis 캐시 무효화
                surveyCacheService.invalidateSurveyCache(id)

                // 게시된 설문조사 목록 캐시도 무효화
                surveyCacheService.invalidatePublishedSurveysCache()

                logger.debug("Survey deleted and all caches invalidated for surveyId: ${id.value}")
            }
    }

    override fun findByIdWithQuestions(id: SurveyId): Mono<Survey> {
        logger.debug("Loading survey with questions for surveyId: ${id.value}")

        val startTime = System.currentTimeMillis()

        // 먼저 Redis 캐시에서 조회
        return surveyCacheService.getSurveyWithQuestionsById(id)
            .flatMap { cachedSurvey ->
                if (cachedSurvey != null) {
                    Mono.just(cachedSurvey)
                } else {
                    // 캐시 미스 시 기존 로직으로 폴백
                    loadSurveyWithJoinQuery(id.value)
                        .flatMap { survey ->
                            // 조회된 데이터를 캐시에 저장
                            surveyCacheService.cacheSurveyWithQuestions(survey)
                                .thenReturn(survey)
                        }
                        .onErrorResume { error ->
                            logger.warn("Join query failed, falling back to batch loading: ${error.message}")
                            // 방법 2: 배치 처리로 폴백
                            loadSurveyWithBatchProcessing(id.value)
                                .flatMap { survey ->
                                    surveyCacheService.cacheSurveyWithQuestions(survey)
                                        .thenReturn(survey)
                                }
                        }
                        .onErrorResume { error ->
                            logger.warn("Batch loading failed, falling back to cached loading: ${error.message}")
                            // 방법 3: 캐시된 DataLoader로 폴백
                            loadSurveyWithCachedDataLoader(id.value)
                                .flatMap { survey ->
                                    surveyCacheService.cacheSurveyWithQuestions(survey)
                                        .thenReturn(survey)
                                }
                        }
                }
            }
            .switchIfEmpty(Mono.error(com.kominioai.global.exception.SurveyNotFoundException("Survey not found with id: ${id.value}")))
            .doOnSuccess { survey ->
                val duration = System.currentTimeMillis() - startTime
                performanceMetrics.recordSurveyLoadingTime("redis", id.value, duration)
                performanceMetrics.recordQueryTime("findByIdWithQuestions", duration)
                logger.debug("Survey loaded with questions in ${duration}ms for surveyId: ${id.value}")
            }
    }

    override fun findAllWithPaging(pageable: Pageable): Mono<Page<Survey>> {
        logger.debug("Loading surveys with paging - page: ${pageable.pageNumber}, size: ${pageable.pageSize}")

        val startTime = System.currentTimeMillis()

        // 페이징 파라미터 계산
        val limit = pageable.pageSize.toLong()
        val offset = (pageable.pageNumber * pageable.pageSize).toLong()

        // 정렬 파라미터 추출
        val sort = pageable.sort.firstOrNull()
        val sortBy = sort?.property ?: "created_at"
        val sortDir = sort?.direction?.name?.lowercase() ?: "desc"

        return Mono.zip(
            // 페이징된 설문지 데이터 조회
            if (sortBy != "created_at" || sortDir != "desc") {
                surveyRepository.findAllWithPagingAndSorting(sortBy, sortDir, limit, offset)
            } else {
                surveyRepository.findAllWithPaging(limit, offset)
            }.collectList(),
            // 전체 개수 조회
            surveyRepository.countAll()
        ).flatMap { tuple ->
            val surveys = tuple.t1
            val totalCount = tuple.t2

            if (surveys.isEmpty()) {
                Mono.just(org.springframework.data.domain.PageImpl<Survey>(
                    emptyList(),
                    pageable,
                    totalCount
                ) as Page<Survey>)
            } else {
                // 페이징된 설문지들의 질문들을 배치로 로드
                val surveyIds = surveys.map { it.id }
                surveyDataLoader.loadSurveysWithQuestionsAndOptions(surveyIds)
                    .map { questionsBySurveyId ->
                        val surveysWithQuestions = surveys.map { surveyEntity ->
                            val questions = questionsBySurveyId[surveyEntity.id] ?: emptyList()
                            surveyEntity.toDomainWithQuestions(questions)
                        }

                        val duration = System.currentTimeMillis() - startTime
                        performanceMetrics.recordBatchProcessingTime(surveys.size, duration)
                        logger.debug("Surveys loaded with paging in ${duration}ms - page: ${pageable.pageNumber}, size: ${pageable.pageSize}, total: $totalCount")

                        org.springframework.data.domain.PageImpl(surveysWithQuestions, pageable, totalCount) as Page<Survey>
                    }
            }
        }
        .onErrorResume { error ->
            logger.error("Error loading surveys with paging: ${error.message}", error)
            Mono.error(error)
        }
    }

    override fun findByStatusWithPaging(status: SurveyStatus, pageable: Pageable): Mono<Page<Survey>> {
        logger.debug("Loading surveys by status with paging - status: $status, page: ${pageable.pageNumber}, size: ${pageable.pageSize}")

        val limit = pageable.pageSize.toLong()
        val offset = (pageable.pageNumber * pageable.pageSize).toLong()

        return Mono.zip(
            surveyRepository.findByStatusWithPaging(status, limit, offset).collectList(),
            surveyRepository.countByStatus(status)
        ).flatMap { tuple ->
            val surveys = tuple.t1
            val totalCount = tuple.t2

            if (surveys.isEmpty()) {
                Mono.just(org.springframework.data.domain.PageImpl<Survey>(emptyList(), pageable, totalCount))
            } else {
                val surveyIds = surveys.map { it.id }
                surveyDataLoader.loadSurveysWithQuestionsAndOptions(surveyIds)
                    .map { questionsBySurveyId ->
                        val surveysWithQuestions = surveys.map { surveyEntity ->
                            val questions = questionsBySurveyId[surveyEntity.id] ?: emptyList()
                            surveyEntity.toDomainWithQuestions(questions)
                        }
                        org.springframework.data.domain.PageImpl(surveysWithQuestions, pageable, totalCount)
                    }
            }
        }
    }

    override fun findByCreatedByWithPaging(userId: UserId, pageable: Pageable): Mono<Page<Survey>> {
        logger.debug("Loading surveys by user with paging - userId: ${userId.value}, page: ${pageable.pageNumber}, size: ${pageable.pageSize}")

        val limit = pageable.pageSize.toLong()
        val offset = (pageable.pageNumber * pageable.pageSize).toLong()

        return Mono.zip(
            surveyRepository.findByCreatedByWithPaging(userId.value, limit, offset).collectList(),
            surveyRepository.countByCreatedBy(userId.value)
        ).flatMap { tuple ->
            val surveys = tuple.t1
            val totalCount = tuple.t2

            if (surveys.isEmpty()) {
                Mono.just(org.springframework.data.domain.PageImpl<Survey>(emptyList(), pageable, totalCount))
            } else {
                val surveyIds = surveys.map { it.id }
                surveyDataLoader.loadSurveysWithQuestionsAndOptions(surveyIds)
                    .map { questionsBySurveyId ->
                        val surveysWithQuestions = surveys.map { surveyEntity ->
                            val questions = questionsBySurveyId[surveyEntity.id] ?: emptyList()
                            surveyEntity.toDomainWithQuestions(questions)
                        }
                        org.springframework.data.domain.PageImpl(surveysWithQuestions, pageable, totalCount)
                    }
            }
        }
    }

    override fun findPublishedSurveysWithPaging(pageable: Pageable): Mono<Page<Survey>> {
        logger.debug("Loading published surveys with paging - page: ${pageable.pageNumber}, size: ${pageable.pageSize}")

        val limit = pageable.pageSize.toLong()
        val offset = (pageable.pageNumber * pageable.pageSize).toLong()

        return Mono.zip(
            surveyRepository.findPublishedSurveysWithPaging(limit, offset).collectList(),
            surveyRepository.countPublishedSurveys()
        ).flatMap { tuple ->
            val surveys = tuple.t1
            val totalCount = tuple.t2

            if (surveys.isEmpty()) {
                Mono.just(org.springframework.data.domain.PageImpl<Survey>(emptyList(), pageable, totalCount))
            } else {
                val surveyIds = surveys.map { it.id }
                surveyDataLoader.loadSurveysWithQuestionsAndOptions(surveyIds)
                    .map { questionsBySurveyId ->
                        val surveysWithQuestions = surveys.map { surveyEntity ->
                            val questions = questionsBySurveyId[surveyEntity.id] ?: emptyList()
                            surveyEntity.toDomainWithQuestions(questions)
                        }
                        org.springframework.data.domain.PageImpl(surveysWithQuestions, pageable, totalCount)
                    }
            }
        }
    }

    // 카운트 메서드들
    override fun countAll(): Mono<Long> = surveyRepository.countAll()

    override fun countByStatus(status: SurveyStatus): Mono<Long> = surveyRepository.countByStatus(status)

    override fun countByCreatedBy(userId: UserId): Mono<Long> = surveyRepository.countByCreatedBy(userId.value)

    override fun countPublishedSurveys(): Mono<Long> = surveyRepository.countPublishedSurveys()

    /**
     * 방법 1: 조인 쿼리를 사용한 최적화된 로딩
     */
    private fun loadSurveyWithJoinQuery(surveyId: String): Mono<Survey> {
        return surveyRepository.findSurveyWithQuestionsAndOptionsTyped(surveyId)
            .collectList()
            .map { joinResults ->
                joinResults.toSurveyWithQuestions()
            }
            .switchIfEmpty(Mono.error(IllegalStateException("Survey not found for id: $surveyId")))
            .flatMap { survey ->
                if (survey != null) Mono.just(survey)
                else Mono.error(IllegalStateException("Survey not found for id: $surveyId"))
            }
            .doOnSuccess {
                logger.debug("Survey loaded with join query for surveyId: $surveyId")
            }
    }

    /**
     * 방법 2: 배치 처리를 사용한 로딩
     */
    private fun loadSurveyWithBatchProcessing(surveyId: String): Mono<Survey> {
        return Mono.zip(
            surveyRepository.findById(surveyId),
            surveyDataLoader.loadSurveyWithQuestionsAndOptions(surveyId)
        ) { surveyEntity, questions ->
            surveyEntity.toDomainWithQuestions(questions)
        }
        .doOnSuccess {
            logger.debug("Survey loaded with batch processing for surveyId: $surveyId")
        }
    }

    /**
     * 방법 3: 캐시된 DataLoader를 사용한 로딩
     */
    private fun loadSurveyWithCachedDataLoader(surveyId: String): Mono<Survey> {
        return Mono.zip(
            surveyRepository.findById(surveyId),
            surveyDataLoader.loadSurveyWithQuestionsAndOptionsCached(surveyId)
        ) { surveyEntity, questions ->
            surveyEntity.toDomainWithQuestions(questions)
        }
        .doOnSuccess {
            logger.debug("Survey loaded with cached data loader for surveyId: $surveyId")
        }
    }

    /**
     * 질문과 옵션들을 배치로 저장
     */
    private fun saveQuestionsAndOptions(survey: Survey, surveyId: String): Mono<Void> {
        if (survey.questions.isEmpty()) {
            return Mono.empty()
        }

        return Flux.fromIterable(survey.questions)
            .flatMap { question ->
                val questionEntity = QuestionEntity.from(question, surveyId)
                questionRepository.save(questionEntity)
                    .flatMap { savedQuestion ->
                        if (question.options.isNotEmpty()) {
                            val optionSaves = question.options.map { option ->
                                val optionEntity = QuestionOptionEntity.from(option, savedQuestion.id)
                                questionOptionRepository.save(optionEntity)
                            }
                            Flux.fromIterable(optionSaves).then()
                        } else {
                            Mono.empty()
                        }
                    }
            }
            .then()
            .doOnSuccess {
                logger.debug("Questions and options saved for surveyId: $surveyId")
            }
    }
}