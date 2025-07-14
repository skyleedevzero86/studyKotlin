package com.kominioai.domain.survey.infrastructure.persistence

import com.kominioai.domain.survey.application.port.out.ParticipationPersistencePort
import com.kominioai.domain.survey.domain.model.SurveyParticipation
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
class ParticipationR2dbcAdapter(
    private val client: DatabaseClient
) : ParticipationPersistencePort {

    private val logger = LoggerFactory.getLogger(ParticipationR2dbcAdapter::class.java)

    @Transactional
    override fun saveParticipation(participation: SurveyParticipation): Mono<Void> {
        val participationEntity = ParticipationEntity.fromDomain(participation)
        
        val insertParticipationSql = """
            INSERT INTO survey_participations (
                id, survey_id, user_id, participant_name, participant_phone, 
                authenticated, status, participated_at, created_at, updated_at
            ) VALUES (
                :id, :surveyId, :userId, :participantName, :participantPhone,
                :authenticated, :status, :participatedAt, :createdAt, :updatedAt
            )
        """.trimIndent()

        return client.sql(insertParticipationSql)
            .bind("id", participationEntity.id ?: "")
            .bind("surveyId", participationEntity.surveyId)
            .bind("userId", participationEntity.userId ?: "")
            .bind("participantName", participationEntity.participantName ?: "")
            .bind("participantPhone", participationEntity.participantPhone ?: "")
            .bind("authenticated", participationEntity.authenticated)
            .bind("status", participationEntity.status)
            .bind("participatedAt", participationEntity.participatedAt)
            .bind("createdAt", participationEntity.createdAt)
            .bind("updatedAt", participationEntity.updatedAt)
            .then()
            .flatMap {
                saveQuestionResponses(participation.responses, participation.id.value)
            }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
            .doOnSuccess {
                logger.info("설문 참여 저장 성공: participationId={}", participation.id.value)
            }
            .doOnError { error ->
                logger.error("설문 참여 저장 실패: participationId={}, error={}", participation.id.value, error.message)
            }
    }

    private fun saveQuestionResponses(
        responses: List<com.kominioai.domain.survey.domain.model.QuestionResponse>,
        participationId: String
    ): Mono<Void> {
        if (responses.isEmpty()) return Mono.empty()

        return Mono.defer {
            val insertResponseSql = """
                INSERT INTO question_responses (
                    id, participation_id, question_id, answer, answer_type, created_at
                ) VALUES (
                    :id, :participationId, :questionId, :answer, :answerType, :createdAt
                )
            """.trimIndent()

            val responseEntities = responses.map { response ->
                QuestionResponseEntity.fromDomain(response, participationId)
            }

            Flux.fromIterable(responseEntities)
                .flatMap { entity ->
                    client.sql(insertResponseSql)
                        .bind("id", entity.id ?: "")
                        .bind("participationId", entity.participationId)
                        .bind("questionId", entity.questionId)
                        .bind("answer", entity.answer ?: "")
                        .bind("answerType", entity.answerType)
                        .bind("createdAt", entity.createdAt)
                        .then()
                }
                .then()
        }
    }

    fun findByParticipationId(participationId: String): Mono<SurveyParticipation> {
        val sql = """
            SELECT p.*, qr.id as response_id, qr.question_id, qr.answer, qr.answer_type
            FROM survey_participations p
            LEFT JOIN question_responses qr ON p.id = qr.participation_id
            WHERE p.id = :participationId
        """.trimIndent()

        return client.sql(sql)
            .bind("participationId", participationId)
            .map { row, _ -> mapRowToParticipationWithResponses(row) }
            .all()
            .collectList()
            .map { participationWithResponses ->
                if (participationWithResponses.isEmpty()) {
                    throw com.kominioai.global.exception.domain.SurveyDomainException.SurveyNotFoundException(
                        com.kominioai.domain.survey.domain.model.SurveyId.fromString(participationId)
                    )
                }

                val participation = participationWithResponses.first().first
                val responses = participationWithResponses.mapNotNull { it.second?.toDomain() }

                com.kominioai.domain.survey.domain.model.SurveyParticipation.reconstruct(
                    id = participation.id ?: "",
                    surveyId = participation.surveyId,
                    participant = com.kominioai.domain.survey.domain.model.ParticipantInfo(
                        userId = participation.userId,
                        name = participation.participantName,
                        phone = participation.participantPhone,
                        authenticated = participation.authenticated
                    ),
                    responses = responses,
                    status = participation.status,
                    participatedAt = participation.participatedAt
                )
            }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
    }

    fun findBySurveyId(surveyId: String): Flux<SurveyParticipation> {
        val sql = """
            SELECT p.*, qr.id as response_id, qr.question_id, qr.answer, qr.answer_type
            FROM survey_participations p
            LEFT JOIN question_responses qr ON p.id = qr.participation_id
            WHERE p.survey_id = :surveyId
            ORDER BY p.participated_at DESC
        """.trimIndent()

        return client.sql(sql)
            .bind("surveyId", surveyId)
            .map { row, _ -> mapRowToParticipationWithResponses(row) }
            .all()
            .groupBy { it.first.id }
            .flatMap { group ->
                group.collectList()
                    .map { participationWithResponses ->
                        val participation = participationWithResponses.first().first
                        val responses = participationWithResponses.mapNotNull { it.second?.toDomain() }

                        com.kominioai.domain.survey.domain.model.SurveyParticipation.reconstruct(
                            id = participation.id ?: "",
                            surveyId = participation.surveyId,
                            participant = com.kominioai.domain.survey.domain.model.ParticipantInfo(
                                userId = participation.userId,
                                name = participation.participantName,
                                phone = participation.participantPhone,
                                authenticated = participation.authenticated
                            ),
                            responses = responses,
                            status = participation.status,
                            participatedAt = participation.participatedAt
                        )
                    }
            }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
    }

    private fun mapRowToParticipationWithResponses(row: Row): Pair<ParticipationEntity, QuestionResponseEntity?> {
        val participation = ParticipationEntity(
            id = row.get("id", String::class.java),
            surveyId = row.get("survey_id", String::class.java) ?: "",
            userId = row.get("user_id", String::class.java),
            participantName = row.get("participant_name", String::class.java),
            participantPhone = row.get("participant_phone", String::class.java),
            authenticated = row.get("authenticated", Boolean::class.java) ?: false,
            status = row.get("status", String::class.java) ?: "",
            participatedAt = row.get("participated_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
            createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
            updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now()
        )

        val responseId = row.get("response_id", String::class.java)
        val questionResponse = if (responseId != null) {
            QuestionResponseEntity(
                id = responseId,
                participationId = participation.id ?: "",
                questionId = row.get("question_id", String::class.java) ?: "",
                answer = row.get("answer", String::class.java),
                answerType = row.get("answer_type", String::class.java) ?: "STRING",
                createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now()
            )
        } else null

        return Pair(participation, questionResponse)
    }

    fun countBySurveyId(surveyId: String): Mono<Long> {
        val sql = "SELECT COUNT(*) FROM survey_participations WHERE survey_id = :surveyId"

        return client.sql(sql)
            .bind("surveyId", surveyId)
            .map { row, _ -> row.get(0, Number::class.java)?.toLong() ?: 0L }
            .one()
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
    }

    fun existsBySurveyIdAndUserId(surveyId: String, userId: String): Mono<Boolean> {
        val sql = "SELECT COUNT(*) FROM survey_participations WHERE survey_id = :surveyId AND user_id = :userId"

        return client.sql(sql)
            .bind("surveyId", surveyId)
            .bind("userId", userId)
            .map { row, _ -> row.get(0, Number::class.java)?.toLong() ?: 0L }
            .one()
            .map { count -> count > 0 }
            .onErrorMap(R2dbcException::class.java) { ex: R2dbcException ->
                InfrastructureException.DatabaseConnectionFailedException(ex)
            }
    }
}