package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.application.port.out.SurveyPersistencePort
import com.kominioai.domain.survey.domain.model.*
import com.kominioai.global.exception.domain.SurveyDomainException
import com.kominioai.global.exception.infrastructure.InfrastructureException
import io.r2dbc.spi.Row
import io.r2dbc.spi.R2dbcException
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class SurveyPersistenceAdapter(
    private val client: DatabaseClient,
    private val queryBuilder: SurveyQueryBuilder,
    private val questionPersistenceAdapter: QuestionPersistenceAdapter
) : SurveyPersistencePort {

    private val logger = LoggerFactory.getLogger(SurveyPersistenceAdapter::class.java)

    override fun findById(id: SurveyId): Mono<Survey> {
        val sql = "SELECT * FROM surveys WHERE id = :id"

        return client.sql(sql)
            .bind("id", id.value)
            .map { row, _ -> mapRowToSurveyEntity(row) }
            .one()
            .switchIfEmpty(
                Mono.error<SurveyEntity>(
                    SurveyDomainException.SurveyNotFoundException(id)
                )
            )
            .flatMap { surveyEntity ->
                questionPersistenceAdapter.findBySurveyId(id)
                    .collectList()
                    .map { questions ->
                        surveyEntity.toDomain().let { survey ->
                            questions.forEach { question ->
                                survey.addQuestion(question)
                            }
                            survey
                        }
                    }
            }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
            .doOnError { error ->
                logger.error("Survey 조회 실패: surveyId={}, error={}", id.value, error.message)
            }
    }

    override fun findAll(criteria: SurveySearchCriteria): Flux<Survey> {
        return queryBuilder.buildSearchQuery(client, criteria)
            .map { row, _ -> mapRowToSurveyEntity(row) }
            .all()
            .flatMap { surveyEntity: SurveyEntity ->
                val surveyId = SurveyId.fromString(surveyEntity.id ?: "")
                questionPersistenceAdapter.findBySurveyId(surveyId)
                    .collectList()
                    .map { questions: List<Question> ->
                        surveyEntity.toDomain().let { survey ->
                            questions.forEach { question ->
                                survey.addQuestion(question)
                            }
                            survey
                        }
                    }
            }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
            .doOnError { error ->
                logger.error("Survey 목록 조회 실패: criteria={}, error={}", criteria, error.message)
            }
    }

    @Transactional
    override fun save(survey: Survey): Mono<SurveyId> {
        val surveyEntity = SurveyEntity.fromDomain(survey)
        val sql = """
            INSERT INTO surveys (
                id, title, author, status, created_at, updated_at, participant_count,
                target_type, start_date, end_date, duration, survey_type, participant_type,
                time_limit_enabled, time_limit_minutes
            ) VALUES (
                :id, :title, :author, :status, :createdAt, :updatedAt, :participantCount,
                :targetType, :startDate, :endDate, :duration, :surveyType, :participantType,
                :timeLimitEnabled, :timeLimitMinutes
            )
        """.trimIndent()

        return client.sql(sql)
            .bind("id", surveyEntity.id ?: "")
            .bind("title", surveyEntity.title)
            .bind("author", surveyEntity.author)
            .bind("status", surveyEntity.status)
            .bind("createdAt", surveyEntity.createdAt)
            .bind("updatedAt", surveyEntity.updatedAt)
            .bind("participantCount", surveyEntity.participantCount)
            .bind("targetType", surveyEntity.targetType)
            .bind("startDate", surveyEntity.startDate ?: LocalDateTime.now())
            .bind("endDate", surveyEntity.endDate ?: LocalDateTime.now())
            .bind("duration", surveyEntity.duration)
            .bind("surveyType", surveyEntity.surveyType)
            .bind("participantType", surveyEntity.participantType)
            .bind("timeLimitEnabled", surveyEntity.timeLimitEnabled ?: false)
            .bind("timeLimitMinutes", surveyEntity.timeLimitMinutes ?: 0)
            .then()
            .thenReturn(survey.id)
            .flatMap { surveyId: SurveyId ->
                val questions = survey.getQuestions()
                if (questions.isNotEmpty()) {
                    questionPersistenceAdapter.saveAll(questions, surveyId)
                        .then(Mono.just(surveyId))
                } else {
                    Mono.just(surveyId)
                }
            }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
            .doOnSuccess { surveyId ->
                logger.info("Survey 저장 성공: surveyId={}", surveyId.value)
            }
            .doOnError { error ->
                logger.error("Survey 저장 실패: survey={}, error={}", survey.id.value, error.message)
            }
    }

    @Transactional
    override fun update(survey: Survey): Mono<SurveyId> {
        val surveyEntity = SurveyEntity.fromDomain(survey)
        val sql = """
            UPDATE surveys SET 
                title = :title, author = :author, status = :status, updated_at = :updatedAt,
                participant_count = :participantCount, target_type = :targetType,
                start_date = :startDate, end_date = :endDate, duration = :duration,
                survey_type = :surveyType, participant_type = :participantType,
                time_limit_enabled = :timeLimitEnabled, time_limit_minutes = :timeLimitMinutes
            WHERE id = :id
        """.trimIndent()

        return client.sql(sql)
            .bind("id", surveyEntity.id ?: "")
            .bind("title", surveyEntity.title)
            .bind("author", surveyEntity.author)
            .bind("status", surveyEntity.status)
            .bind("updatedAt", surveyEntity.updatedAt)
            .bind("participantCount", surveyEntity.participantCount)
            .bind("targetType", surveyEntity.targetType)
            .bind("startDate", surveyEntity.startDate ?: LocalDateTime.now())
            .bind("endDate", surveyEntity.endDate ?: LocalDateTime.now())
            .bind("duration", surveyEntity.duration)
            .bind("surveyType", surveyEntity.surveyType)
            .bind("participantType", surveyEntity.participantType)
            .bind("timeLimitEnabled", surveyEntity.timeLimitEnabled ?: false)
            .bind("timeLimitMinutes", surveyEntity.timeLimitMinutes ?: 0)
            .then()
            .thenReturn(survey.id)
            .flatMap { surveyId: SurveyId ->
                questionPersistenceAdapter.deleteBySurveyId(surveyId)
                    .then(
                        Mono.defer {
                            val questions = survey.getQuestions()
                            if (questions.isNotEmpty()) {
                                questionPersistenceAdapter.saveAll(questions, surveyId)
                                    .then(Mono.empty<Void>())
                            } else {
                                Mono.empty<Void>()
                            }
                        }
                    )
                    .thenReturn(surveyId)
            }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
            .doOnSuccess { surveyId ->
                logger.info("Survey 수정 성공: surveyId={}", surveyId.value)
            }
            .doOnError { error ->
                logger.error("Survey 수정 실패: survey={}, error={}", survey.id.value, error.message)
            }
    }

    @Transactional
    override fun deleteById(id: SurveyId): Mono<Void> {
        return questionPersistenceAdapter.deleteBySurveyId(id)
            .then(
                Mono.defer {
                    val sql = "DELETE FROM surveys WHERE id = :id"
                    client.sql(sql)
                        .bind("id", id.value)
                        .then()
                }
            )
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
            .doOnSuccess {
                logger.info("Survey 삭제 성공: surveyId={}", id.value)
            }
            .doOnError { error ->
                logger.error("Survey 삭제 실패: surveyId={}, error={}", id.value, error.message)
            }
    }

    @Transactional
    override fun deleteByIds(ids: List<SurveyId>): Mono<Void> {
        if (ids.isEmpty()) return Mono.empty<Void>()

        return Flux.fromIterable(ids)
            .flatMap { surveyId: SurveyId ->
                questionPersistenceAdapter.deleteBySurveyId(surveyId)
                    .then(
                        Mono.defer {
                            val sql = "DELETE FROM surveys WHERE id = :id"
                            client.sql(sql)
                                .bind("id", surveyId.value)
                                .then()
                        }
                    )
            }
            .then()
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
            .doOnSuccess {
                logger.info("Survey 일괄 삭제 성공: surveyIds={}", ids.map { it.value })
            }
            .doOnError { error ->
                logger.error("Survey 일괄 삭제 실패: surveyIds={}, error={}", ids.map { it.value }, error.message)
            }
    }

    override fun count(criteria: SurveySearchCriteria): Mono<Long> {
        return queryBuilder.buildCountQuery(client, criteria)
            .map { row, _ -> row.get(0, Number::class.java)?.toLong() ?: 0L }
            .one()
            .doOnError { error ->
                logger.error("Survey 개수 조회 실패: criteria={}, error={}", criteria, error.message)
            }
    }

    override fun existsById(id: SurveyId): Mono<Boolean> {
        val sql = "SELECT COUNT(*) FROM surveys WHERE id = :id"

        return client.sql(sql)
            .bind("id", id.value)
            .map { row, _ -> row.get(0, Number::class.java)?.toLong() ?: 0L }
            .one()
            .map { count -> count > 0 }
            .doOnError { error ->
                logger.error("Survey 존재 여부 확인 실패: surveyId={}, error={}", id.value, error.message)
            }
    }

    private fun mapRowToSurveyEntity(row: Row): SurveyEntity {
        return SurveyEntity(
            id = row.get("id", String::class.java),
            title = row.get("title", String::class.java) ?: "",
            author = row.get("author", String::class.java) ?: "",
            status = row.get("status", String::class.java) ?: "",
            createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
            updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
            participantCount = row.get("participant_count", Int::class.java) ?: 0,
            targetType = row.get("target_type", String::class.java) ?: "",
            startDate = row.get("start_date", LocalDateTime::class.java),
            endDate = row.get("end_date", LocalDateTime::class.java),
            duration = row.get("duration", String::class.java) ?: "0", // Int -> String으로 변경
            surveyType = row.get("survey_type", String::class.java) ?: "",
            participantType = row.get("participant_type", String::class.java) ?: "",
            timeLimitEnabled = row.get("time_limit_enabled", Boolean::class.java),
            timeLimitMinutes = row.get("time_limit_minutes", Int::class.java)
        )
    }
}