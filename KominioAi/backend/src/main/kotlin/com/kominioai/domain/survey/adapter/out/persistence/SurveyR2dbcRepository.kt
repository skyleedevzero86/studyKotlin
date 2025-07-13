package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.adapter.out.monitoring.ExcelExportService
import com.kominioai.domain.survey.application.dto.SurveyResult
import com.kominioai.domain.survey.domain.model.Author
import com.kominioai.domain.survey.domain.model.ParticipantType
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyPeriod
import com.kominioai.domain.survey.domain.model.SurveyStatus
import com.kominioai.domain.survey.domain.model.SurveyType
import com.kominioai.domain.survey.domain.model.TargetType
import com.kominioai.domain.survey.domain.repository.SurveyRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class SurveyR2dbcRepository(
    private val client: DatabaseClient,
    private val excelExportService: ExcelExportService
) : SurveyRepository {

    override fun findAll(title: String?, author: String?, status: SurveyStatus?, page: Int, size: Int): Flux<Survey> {
        val sql = StringBuilder("SELECT * FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any?>()
        title?.let { sql.append(" AND title LIKE :title"); params["title"] = "%$it%" }
        author?.let { sql.append(" AND author = :author"); params["author"] = it }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }
        sql.append(" ORDER BY id DESC LIMIT :size OFFSET :offset")
        params["size"] = size
        params["offset"] = (page - 1) * size

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row, _ ->
            val startDate = row.get("start_date", LocalDateTime::class.java)
            val endDate = row.get("end_date", LocalDateTime::class.java)

            Survey(
                id = (row.get("id", Number::class.java)?.toLong() ?: 0L),
                title = row.get("title", String::class.java) ?: "",
                author = Author(row.get("author", String::class.java) ?: ""),
                status = SurveyStatus.valueOf(row.get("status", String::class.java) ?: "PENDING"),
                createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                participantCount = row.get("participant_count", Number::class.java)?.toInt() ?: 0,
                targetType = TargetType.valueOf(row.get("target_type", String::class.java) ?: "ALL"),
                startDate = startDate,
                endDate = endDate,
                duration = row.get("duration", String::class.java) ?: "",
                surveyType = SurveyType.valueOf(row.get("survey_type", String::class.java) ?: "SURVEY"),
                participantType = ParticipantType.valueOf(row.get("participant_type", String::class.java) ?: "MEMBER"),
                timeLimit = null,
                period = SurveyPeriod(
                    startDate ?: LocalDateTime.now(),
                    endDate ?: LocalDateTime.now()
                ),
                questions = emptyList()
            )
        }.all()
    }

    override fun count(title: String?, author: String?, status: SurveyStatus?): Mono<Long> {
        val sql = StringBuilder("SELECT COUNT(*) FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any?>()
        title?.let { sql.append(" AND title LIKE :title"); params["title"] = "%$it%" }
        author?.let { sql.append(" AND author = :author"); params["author"] = it }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row, _ -> row.get(0, Number::class.java)?.toLong() ?: 0L }.one()
    }

    override fun findById(id: Long): Mono<Survey> {
        val sql = "SELECT * FROM surveys WHERE id = :id"
        val params = mapOf("id" to id)

        return bindParams(client.sql(sql), params)
            .map { row, _ ->
                val startDate = row.get("start_date", LocalDateTime::class.java)
                val endDate = row.get("end_date", LocalDateTime::class.java)

                Survey(
                    id = (row.get("id", Number::class.java)?.toLong() ?: 0L),
                    title = row.get("title", String::class.java) ?: "",
                    author = Author(row.get("author", String::class.java) ?: ""),
                    status = SurveyStatus.valueOf(row.get("status", String::class.java) ?: "PENDING"),
                    createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                    updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                    participantCount = row.get("participant_count", Number::class.java)?.toInt() ?: 0,
                    targetType = TargetType.valueOf(row.get("target_type", String::class.java) ?: "ALL"),
                    startDate = startDate,
                    endDate = endDate,
                    duration = row.get("duration", String::class.java) ?: "",
                    surveyType = SurveyType.valueOf(row.get("survey_type", String::class.java) ?: "SURVEY"),
                    participantType = ParticipantType.valueOf(
                        row.get("participant_type", String::class.java) ?: "MEMBER"
                    ),
                    timeLimit = null,
                    period = SurveyPeriod(
                        startDate ?: LocalDateTime.now(),
                        endDate ?: LocalDateTime.now()
                    ),
                    questions = emptyList()
                )
            }.one()
    }

    override fun save(survey: Survey): Mono<Long> {
        val sql = """
            INSERT INTO surveys (title, author, status, created_at, updated_at, participant_count, target_type, start_date, end_date, duration, survey_type, participant_type)
            VALUES (:title, :author, :status, :createdAt, :updatedAt, :participantCount, :targetType, :startDate, :endDate, :duration, :surveyType, :participantType)
            RETURNING id
        """.trimIndent()

        val params = mutableMapOf<String, Any?>()
        params["title"] = survey.title
        params["author"] = survey.author.name
        params["status"] = survey.status.name
        params["createdAt"] = survey.createdAt
        params["updatedAt"] = survey.updatedAt
        params["participantCount"] = survey.participantCount
        params["targetType"] = survey.targetType.name
        params["startDate"] = survey.startDate
        params["endDate"] = survey.endDate
        params["duration"] = survey.duration
        params["surveyType"] = survey.surveyType.name
        params["participantType"] = survey.participantType.name

        return bindParams(client.sql(sql), params)
            .map { row, _ -> row.get("id", Number::class.java)?.toLong() ?: 0L }
            .one()
    }

    override fun update(survey: Survey): Mono<Long> {
        val surveyId = survey.id ?: throw IllegalArgumentException("Survey ID cannot be null for update")

        val sql = """
            UPDATE surveys SET title=:title, updated_at=:updatedAt, start_date=:startDate, end_date=:endDate, survey_type=:surveyType, participant_type=:participantType
            WHERE id=:id
        """.trimIndent()

        val params = mutableMapOf<String, Any?>()
        params["title"] = survey.title
        params["updatedAt"] = survey.updatedAt
        params["startDate"] = survey.startDate
        params["endDate"] = survey.endDate
        params["surveyType"] = survey.surveyType.name
        params["participantType"] = survey.participantType.name
        params["id"] = surveyId

        return bindParams(client.sql(sql), params)
            .fetch().rowsUpdated()
            .map { surveyId }
    }

    override fun deleteByIds(ids: List<Long>): Mono<Void> {
        if (ids.isEmpty()) return Mono.empty()

        return Flux.fromIterable(ids)
            .flatMap { id ->
                val sql = "DELETE FROM surveys WHERE id = :id"
                val params = mapOf("id" to id)
                bindParams(client.sql(sql), params).then()
            }
            .then()
    }

    override fun findSurveyResults(surveyId: Long): Mono<ByteArray> {

        val sql = "SELECT question_order, question_content, answer FROM survey_results WHERE survey_id = :surveyId"
        val params = mapOf("surveyId" to surveyId)

        return bindParams(client.sql(sql), params)
            .map { row, _ ->
                SurveyResult(
                    questionOrder = row.get("question_order", Number::class.java)?.toInt() ?: 0,
                    questionContent = row.get("question_content", String::class.java) ?: "",
                    answer = row.get("answer", String::class.java) ?: ""
                )
            }
            .all()
            .collectList()
            .map { results -> excelExportService.generateSurveyResultsExcel(results) }
    }

    private fun bindParams(
        spec: DatabaseClient.GenericExecuteSpec,
        params: Map<String, Any?>
    ): DatabaseClient.GenericExecuteSpec {
        var s = spec
        params.forEach { (k, v) ->
            s = if (v != null) {
                s.bind(k, v)
            } else {
                s.bindNull(k, LocalDateTime::class.java)
            }
        }
        return s
    }
}