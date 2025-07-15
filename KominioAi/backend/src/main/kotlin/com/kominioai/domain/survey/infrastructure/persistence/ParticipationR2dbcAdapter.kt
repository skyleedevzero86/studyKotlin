package com.kominioai.domain.survey.infrastructure.persistence

import com.kominioai.domain.survey.application.port.out.ParticipationPersistencePort
import com.kominioai.domain.survey.domain.model.SurveyParticipation
import io.r2dbc.spi.Row
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

        val participationSql = """
            INSERT INTO survey_participations (
                id, survey_id, user_id, participant_name, participant_phone,
                authenticated, status, participated_at, created_at, updated_at
            ) VALUES (
                :id, :surveyId, :userId, :participantName, :participantPhone,
                :authenticated, :status, :participatedAt, :createdAt, :updatedAt
            )
        """.trimIndent()

        return client.sql(participationSql)
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
                val responses = participation.responses
                if (responses.isNotEmpty()) {
                    saveQuestionResponses(responses, participation.id.value)
                } else {
                    Mono.empty<Void>()
                }
            }
            .doOnSuccess {
                logger.info("참여 저장 성공: participationId={}, surveyId={}",
                    participation.id.value, participation.surveyId.value)
            }
            .doOnError { error ->
                logger.error("참여 저장 실패: participationId={}, error={}",
                    participation.id.value, error.message)
            }
    }

    override fun findBySurveyId(surveyId: String): Flux<SurveyParticipation> {
        val sql = """
            SELECT sp.*, qr.id as response_id, qr.question_id, qr.answer, qr.answer_type
            FROM survey_participations sp
            LEFT JOIN question_responses qr ON sp.id = qr.participation_id
            WHERE sp.survey_id = :surveyId
            ORDER BY sp.participated_at DESC, qr.question_id
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
                        val responses = participationWithResponses.mapNotNull { it.second }

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
            .doOnError { error ->
                logger.error("참여 목록 조회 실패: surveyId={}, error={}", surveyId, error.message)
            }
    }

    override fun countBySurveyId(surveyId: String): Mono<Long> {
        val sql = "SELECT COUNT(*) FROM survey_participations WHERE survey_id = :surveyId"

        return client.sql(sql)
            .bind("surveyId", surveyId)
            .map { row, _ -> row.get(0, Number::class.java)?.toLong() ?: 0L }
            .one()
            .doOnError { error ->
                logger.error("참여자 수 조회 실패: surveyId={}, error={}", surveyId, error.message)
            }
    }

    private fun saveQuestionResponses(responses: List<com.kominioai.domain.survey.domain.model.QuestionResponse>, participationId: String): Mono<Void> {
        return Flux.fromIterable(responses)
            .flatMap { response ->
                val responseEntity = QuestionResponseEntity.fromDomain(response, participationId)
                val sql = """
                    INSERT INTO question_responses (
                        id, participation_id, question_id, answer, answer_type, created_at
                    ) VALUES (
                        :id, :participationId, :questionId, :answer, :answerType, :createdAt
                    )
                """.trimIndent()

                client.sql(sql)
                    .bind("id", responseEntity.id ?: "")
                    .bind("participationId", responseEntity.participationId)
                    .bind("questionId", responseEntity.questionId)
                    .bind("answer", responseEntity.answer ?: "")
                    .bind("answerType", responseEntity.answerType)
                    .bind("createdAt", responseEntity.createdAt)
                    .then()
            }
            .then()
    }

    private fun mapRowToParticipationWithResponses(row: Row): Pair<ParticipationEntity, com.kominioai.domain.survey.domain.model.QuestionResponse?> {
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
        val response = if (responseId != null) {
            val questionResponseEntity = QuestionResponseEntity(
                id = responseId,
                participationId = participation.id ?: "",
                questionId = row.get("question_id", String::class.java) ?: "",
                answer = row.get("answer", String::class.java),
                answerType = row.get("answer_type", String::class.java) ?: "STRING",
                createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now()
            )
            questionResponseEntity.toDomain()
        } else null

        return Pair(participation, response)
    }
}