package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.application.port.output.SurveyStatisticsRepository
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import com.kominioai.domain.survey.presentation.rest.dto.response.QuestionStatisticsDto
import com.kominioai.domain.survey.presentation.rest.dto.response.OptionStatisticsDto
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionOptionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import io.r2dbc.spi.Row
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class R2dbcSurveyStatisticsRepositoryAdapter(
    private val databaseClient: DatabaseClient
) : SurveyStatisticsRepository {

    private val logger = LoggerFactory.getLogger(R2dbcSurveyStatisticsRepositoryAdapter::class.java)

    override fun getSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto> {
        val startTime = System.currentTimeMillis()

        return databaseClient.sql("""
            WITH survey_info AS (
                SELECT 
                    s.id as survey_id,
                    s.title,
                    s.status,
                    COUNT(DISTINCT q.id) as question_count
                FROM surveys s
                LEFT JOIN questions q ON s.id = q.survey_id
                WHERE s.id = $1
                GROUP BY s.id, s.title, s.status
            ),
            response_stats AS (
                SELECT 
                    COUNT(DISTINCT sr.id) as total_responses
                FROM survey_responses sr
                WHERE sr.survey_id = $1
            ),
            question_stats AS (
                SELECT 
                    q.id as question_id,
                    q.text as question_text,
                    q.type as question_type,
                    COUNT(DISTINCT ra.response_id) as answer_count
                FROM questions q
                LEFT JOIN response_answers ra ON q.id = ra.question_id
                WHERE q.survey_id = $1
                GROUP BY q.id, q.text, q.type
            ),
            option_stats AS (
                SELECT 
                    qo.question_id,
                    qo.id as option_id,
                    qo.text as option_text,
                    COUNT(ra.response_id) as selection_count
                FROM question_options qo
                LEFT JOIN response_answers ra ON qo.id = ANY(ra.selected_option_ids)
                WHERE qo.question_id IN (
                    SELECT id FROM questions WHERE survey_id = $1
                )
                GROUP BY qo.question_id, qo.id, qo.text
            )
            SELECT 
                si.survey_id,
                si.title,
                si.status,
                si.question_count,
                rs.total_responses,
                qs.question_id,
                qs.question_text,
                qs.question_type,
                qs.answer_count,
                os.option_id,
                os.option_text,
                os.selection_count
            FROM survey_info si
            CROSS JOIN response_stats rs
            LEFT JOIN question_stats qs ON true
            LEFT JOIN option_stats os ON qs.question_id = os.question_id
            ORDER BY qs.question_id, os.option_id
        """)
            .bind("$1", surveyId.value)
            .map { readable ->
                mapRowToStatisticsDto(readable as Row)
            }
            .all()
            .collectList()
            .map { rows ->
                aggregateStatisticsData(rows, surveyId)
            }
            .doOnSuccess { statistics ->
                val duration = System.currentTimeMillis() - startTime
                logger.debug("Survey statistics retrieved in ${duration}ms for surveyId: ${surveyId.value}")
            }
            .doOnError { error ->
                logger.error("Error retrieving survey statistics for surveyId: ${surveyId.value}", error)
            }
    }

    override fun getSurveyStatisticsBatch(surveyIds: List<SurveyId>): Mono<Map<SurveyId, SurveyStatisticsDto>> {
        if (surveyIds.isEmpty()) {
            return Mono.just(emptyMap())
        }

        val placeholders = surveyIds.indices.joinToString(",") { "$${it + 1}" }

        return databaseClient.sql("""
            WITH survey_responses_count AS (
                SELECT 
                    sr.survey_id,
                    COUNT(DISTINCT sr.id) as total_responses
                FROM survey_responses sr
                WHERE sr.survey_id IN ($placeholders)
                GROUP BY sr.survey_id
            ),
            question_responses_count AS (
                SELECT 
                    q.survey_id,
                    q.id as question_id,
                    COUNT(DISTINCT ra.response_id) as answer_count
                FROM questions q
                LEFT JOIN response_answers ra ON q.id = ra.question_id
                WHERE q.survey_id IN ($placeholders)
                GROUP BY q.survey_id, q.id
            )
            SELECT 
                s.id as survey_id,
                s.title,
                s.status,
                COALESCE(src.total_responses, 0) as total_responses,
                qrc.question_id,
                qrc.answer_count
            FROM surveys s
            LEFT JOIN survey_responses_count src ON s.id = src.survey_id
            LEFT JOIN question_responses_count qrc ON s.id = qrc.survey_id
            WHERE s.id IN ($placeholders)
            ORDER BY s.id, qrc.question_id
        """)
            .bindValues(surveyIds.mapIndexed { index, surveyId ->
                "${index + 1}" to surveyId.value
            }.toMap())
            .map { readable ->
                mapRowToBatchStatisticsDto(readable as Row)
            }
            .all()
            .collectList()
            .map { rows ->
                aggregateBatchStatisticsData(rows)
            }
    }

    override fun refreshSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto> {
        return getSurveyStatistics(surveyId)
            .doOnSuccess {
                logger.info("Survey statistics refreshed for surveyId: ${surveyId.value}")
            }
    }

    private fun mapRowToStatisticsDto(row: Row): StatisticsRow {
        return StatisticsRow(
            surveyId = row.get("survey_id", String::class.java) ?: "",
            title = row.get("title", String::class.java) ?: "",
            status = row.get("status", String::class.java) ?: "",
            questionCount = row.get("question_count", Int::class.java) ?: 0,
            totalResponses = row.get("total_responses", Long::class.java) ?: 0L,
            questionId = row.get("question_id", String::class.java),
            questionText = row.get("question_text", String::class.java),
            questionType = row.get("question_type", String::class.java),
            answerCount = row.get("answer_count", Long::class.java) ?: 0L,
            optionId = row.get("option_id", String::class.java),
            optionText = row.get("option_text", String::class.java),
            selectionCount = row.get("selection_count", Long::class.java) ?: 0L
        )
    }

    private fun mapRowToBatchStatisticsDto(row: Row): BatchStatisticsRow {
        return BatchStatisticsRow(
            surveyId = row.get("survey_id", String::class.java) ?: "",
            title = row.get("title", String::class.java) ?: "",
            status = row.get("status", String::class.java) ?: "",
            totalResponses = row.get("total_responses", Long::class.java) ?: 0L,
            questionId = row.get("question_id", String::class.java),
            answerCount = row.get("answer_count", Long::class.java) ?: 0L
        )
    }

    private fun aggregateStatisticsData(rows: List<StatisticsRow>, surveyId: SurveyId): SurveyStatisticsDto {
        if (rows.isEmpty()) {
            return SurveyStatisticsDto(
                surveyId = surveyId,
                title = "",
                responseCount = 0,
                questionStatistics = emptyList()
            )
        }

        val firstRow = rows.first()
        val questionStatsMap = mutableMapOf<String, MutableList<StatisticsRow>>()

        rows.forEach { row ->
            if (row.questionId != null) {
                questionStatsMap.getOrPut(row.questionId) { mutableListOf() }.add(row)
            }
        }

        val questionStatistics = questionStatsMap.map { (questionId, questionRows) ->
            val firstQuestionRow = questionRows.first()
            val optionStatistics = questionRows
                .filter { it.optionId != null }
                .map { row ->
                    OptionStatisticsDto(
                        optionId = QuestionOptionId.from(row.optionId!!),
                        text = row.optionText ?: "",
                        count = row.selectionCount.toInt()
                    )
                }

            QuestionStatisticsDto(
                questionId = QuestionId.from(questionId),
                text = firstQuestionRow.questionText ?: "",
                type = QuestionType.valueOf(firstQuestionRow.questionType ?: "TEXT"),
                totalAnswers = firstQuestionRow.answerCount.toInt(),
                optionStatistics = optionStatistics
            )
        }

        val surveyIdFromRow = SurveyId.from(firstRow.surveyId)

        return SurveyStatisticsDto(
            surveyId = surveyIdFromRow,
            title = firstRow.title,
            responseCount = firstRow.totalResponses.toInt(),
            questionStatistics = questionStatistics
        )
    }

    private fun aggregateBatchStatisticsData(rows: List<BatchStatisticsRow>): Map<SurveyId, SurveyStatisticsDto> {
        // 처음부터 SurveyId를 키로 사용하는 Map 생성
        val surveyMap = mutableMapOf<SurveyId, MutableList<BatchStatisticsRow>>()

        rows.forEach { row ->
            val surveyId = SurveyId.from(row.surveyId)
            surveyMap.getOrPut(surveyId) { mutableListOf() }.add(row)
        }

        return surveyMap.mapValues { (surveyId, surveyRows) ->
            val firstRow = surveyRows.first()
            val questionStatistics = surveyRows
                .filter { it.questionId != null }
                .groupBy { it.questionId }
                .map { (questionId, questionRows) ->
                    QuestionStatisticsDto(
                        questionId = QuestionId.from(questionId!!),
                        text = "", // 배치 조회에서는 질문 텍스트를 별도로 조회해야 함
                        type = QuestionType.TEXT, // 배치 조회에서는 질문 타입을 별도로 조회해야 함
                        totalAnswers = questionRows.first().answerCount.toInt(),
                        optionStatistics = emptyList() // 배치 조회에서는 옵션 통계를 별도로 조회해야 함
                    )
                }

            SurveyStatisticsDto(
                surveyId = surveyId, // 이미 SurveyId 타입이므로 변환 불필요
                title = firstRow.title,
                responseCount = firstRow.totalResponses.toInt(),
                questionStatistics = questionStatistics
            )
        }
    }

    private data class StatisticsRow(
        val surveyId: String,
        val title: String,
        val status: String,
        val questionCount: Int,
        val totalResponses: Long,
        val questionId: String?,
        val questionText: String?,
        val questionType: String?,
        val answerCount: Long,
        val optionId: String?,
        val optionText: String?,
        val selectionCount: Long
    )

    private data class BatchStatisticsRow(
        val surveyId: String,
        val title: String,
        val status: String,
        val totalResponses: Long,
        val questionId: String?,
        val answerCount: Long
    )
}