package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.util.StructuredLogging
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/admin/cache")
@PreAuthorize("hasRole('ADMIN')")
class SurveyCacheController(
    private val surveyCacheService: SurveyCacheService
) {
    
    private val logger = LoggerFactory.getLogger(SurveyCacheController::class.java)

    @GetMapping("/stats")
    fun getCacheStats(): Mono<ResponseEntity<Map<String, Any>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Cache statistics retrieval started",
            "operation" to "GET_CACHE_STATS"
        )
        
        return surveyCacheService.getCacheStats()
            .map { stats ->
                val duration = System.currentTimeMillis() - startTime
                
                StructuredLogging.logInfo(
                    logger = logger,
                    message = "Cache statistics retrieved successfully",
                    "operation" to "GET_CACHE_STATS",
                    "duration" to duration,
                    "statsKeys" to stats.keys.size,
                    "hitRate" to stats["hitRate"],
                    "totalRequests" to stats["totalRequests"]
                )
                
                ResponseEntity.ok(stats)
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                
                StructuredLogging.logError(
                    logger = logger,
                    message = "Cache statistics retrieval failed",
                    throwable = error,
                    "operation" to "GET_CACHE_STATS",
                    "duration" to duration
                )
                
                Mono.error(error)
            }
    }

    @DeleteMapping("/surveys/{surveyId}")
    fun invalidateSurveyCache(@PathVariable surveyId: String): Mono<ResponseEntity<Map<String, String>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Survey cache invalidation started",
            "operation" to "INVALIDATE_SURVEY_CACHE",
            "surveyId" to surveyId
        )
        
        return surveyCacheService.invalidateSurveyCache(SurveyId.from(surveyId))
            .map { success ->
                val duration = System.currentTimeMillis() - startTime
                
                if (success) {
                    StructuredLogging.logInfo(
                        logger = logger,
                        message = "Survey cache invalidated successfully",
                        "operation" to "INVALIDATE_SURVEY_CACHE",
                        "surveyId" to surveyId,
                        "duration" to duration,
                        "result" to "SUCCESS"
                    )
                    ResponseEntity.ok(mapOf("message" to "Survey cache invalidated successfully"))
                } else {
                    StructuredLogging.logWarn(
                        logger = logger,
                        message = "Survey cache invalidation failed",
                        "operation" to "INVALIDATE_SURVEY_CACHE",
                        "surveyId" to surveyId,
                        "duration" to duration,
                        "result" to "FAILED"
                    )
                    ResponseEntity.internalServerError().body(mapOf("message" to "Failed to invalidate survey cache"))
                }
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                
                StructuredLogging.logError(
                    logger = logger,
                    message = "Survey cache invalidation failed with error",
                    throwable = error,
                    "operation" to "INVALIDATE_SURVEY_CACHE",
                    "surveyId" to surveyId,
                    "duration" to duration
                )
                
                Mono.error(error)
            }
    }

    @DeleteMapping("/surveys/published")
    fun invalidatePublishedSurveysCache(): Mono<ResponseEntity<Map<String, String>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Published surveys cache invalidation started",
            "operation" to "INVALIDATE_PUBLISHED_SURVEYS_CACHE"
        )
        
        return surveyCacheService.invalidatePublishedSurveysCache()
            .map { success ->
                val duration = System.currentTimeMillis() - startTime
                
                if (success) {
                    StructuredLogging.logInfo(
                        logger = logger,
                        message = "Published surveys cache invalidated successfully",
                        "operation" to "INVALIDATE_PUBLISHED_SURVEYS_CACHE",
                        "duration" to duration,
                        "result" to "SUCCESS"
                    )
                    ResponseEntity.ok(mapOf("message" to "Published surveys cache invalidated successfully"))
                } else {
                    StructuredLogging.logWarn(
                        logger = logger,
                        message = "Published surveys cache invalidation failed",
                        "operation" to "INVALIDATE_PUBLISHED_SURVEYS_CACHE",
                        "duration" to duration,
                        "result" to "FAILED"
                    )
                    ResponseEntity.internalServerError().body(mapOf("message" to "Failed to invalidate published surveys cache"))
                }
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                
                StructuredLogging.logError(
                    logger = logger,
                    message = "Published surveys cache invalidation failed with error",
                    throwable = error,
                    "operation" to "INVALIDATE_PUBLISHED_SURVEYS_CACHE",
                    "duration" to duration
                )
                
                Mono.error(error)
            }
    }

    @DeleteMapping("/surveys")
    fun invalidateAllSurveyCache(): Mono<ResponseEntity<Map<String, String>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "All survey cache invalidation started",
            "operation" to "INVALIDATE_ALL_SURVEY_CACHE"
        )
        
        return surveyCacheService.invalidateAllSurveyCache()
            .map { success ->
                val duration = System.currentTimeMillis() - startTime
                
                if (success) {
                    StructuredLogging.logInfo(
                        logger = logger,
                        message = "All survey cache invalidated successfully",
                        "operation" to "INVALIDATE_ALL_SURVEY_CACHE",
                        "duration" to duration,
                        "result" to "SUCCESS"
                    )
                    ResponseEntity.ok(mapOf("message" to "All survey cache invalidated successfully"))
                } else {
                    StructuredLogging.logWarn(
                        logger = logger,
                        message = "All survey cache invalidation failed",
                        "operation" to "INVALIDATE_ALL_SURVEY_CACHE",
                        "duration" to duration,
                        "result" to "FAILED"
                    )
                    ResponseEntity.internalServerError().body(mapOf("message" to "Failed to invalidate all survey cache"))
                }
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                
                StructuredLogging.logError(
                    logger = logger,
                    message = "All survey cache invalidation failed with error",
                    throwable = error,
                    "operation" to "INVALIDATE_ALL_SURVEY_CACHE",
                    "duration" to duration
                )
                
                Mono.error(error)
            }
    }
} 