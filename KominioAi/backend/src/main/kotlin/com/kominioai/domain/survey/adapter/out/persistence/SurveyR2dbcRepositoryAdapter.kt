package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.application.port.out.LoadSurveyPort
import com.kominioai.domain.survey.application.port.out.SaveSurveyPort
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.domain.survey.domain.model.SurveyStatus
import org.springframework.r2dbc.core.DatabaseClient
import io.r2dbc.spi.Row
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class SurveyR2dbcRepositoryAdapter(
    private val client: DatabaseClient
) : LoadSurveyPort, SaveSurveyPort {

    override fun loadSurvey(surveyId: SurveyId): Mono<Survey> {
        val sql = "SELECT * FROM surveys WHERE id = :id"
        return client.sql(sql)
            .bind("id", surveyId.value)
            .map { row, _ -> mapRowToSurvey(row) }
            .one()
    }

    override fun loadSurveys(page: Int, size: Int): Flux<Survey> {
        val sql = "SELECT * FROM surveys ORDER BY created_at DESC LIMIT :size OFFSET :offset"
        return client.sql(sql)
            .bind("size", size)
            .bind("offset", (page - 1) * size)
            .map { row, _ -> mapRowToSurvey(row) }
            .all()
    }

    override fun loadSurveysByAuthor(author: String, page: Int, size: Int): Flux<Survey> {
        val sql = "SELECT * FROM surveys WHERE author = :author ORDER BY created_at DESC LIMIT :size OFFSET :offset"
        return client.sql(sql)
            .bind("author", author)
            .bind("size", size)
            .bind("offset", (page - 1) * size)
            .map { row, _ -> mapRowToSurvey(row) }
            .all()
    }

    override fun loadSurveysByStatus(status: SurveyStatus, page: Int, size: Int): Flux<Survey> {
        val sql = "SELECT * FROM surveys WHERE status = :status ORDER BY created_at DESC LIMIT :size OFFSET :offset"
        return client.sql(sql)
            .bind("status", status.name)
            .bind("size", size)
            .bind("offset", (page - 1) * size)
            .map { row, _ -> mapRowToSurvey(row) }
            .all()
    }

    override fun loadSurveysByTitle(title: String, page: Int, size: Int): Flux<Survey> {
        val sql = "SELECT * FROM surveys WHERE title ILIKE :title ORDER BY created_at DESC LIMIT :size OFFSET :offset"
        return client.sql(sql)
            .bind("title", "%$title%")
            .bind("size", size)
            .bind("offset", (page - 1) * size)
            .map { row, _ -> mapRowToSurvey(row) }
            .all()
    }

    override fun countSurveys(title: String?, author: String?, status: SurveyStatus?): Mono<Long> {
        val sql = StringBuilder("SELECT COUNT(*) FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any?>()
        
        title?.let { sql.append(" AND title ILIKE :title"); params["title"] = "%$it%" }
        author?.let { sql.append(" AND author = :author"); params["author"] = it }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }

        var spec = client.sql(sql.toString())
        params.forEach { (key, value) ->
            spec = if (value != null) {
                spec.bind(key, value)
            } else {
                spec.bindNull(key, String::class.java)
            }
        }

        return spec.map { row, _ -> row.get(0, Number::class.java)?.toLong() ?: 0L }.one()
    }

    override fun saveSurvey(survey: Survey): Mono<SurveyId> {
        val sql = """
            INSERT INTO surveys (id, title, author, status, start_date, end_date, participant_count, target_type, survey_type, participant_type, created_at, updated_at)
            VALUES (:id, :title, :author, :status, :startDate, :endDate, :participantCount, :targetType, :surveyType, :participantType, :createdAt, :updatedAt)
        """.trimIndent()

        return client.sql(sql)
            .bind("id", survey.id.value)
            .bind("title", survey.title.value)
            .bind("author", survey.author.name)
            .bind("status", survey.status.name)
            .bind("startDate", survey.period.startDate)
            .bind("endDate", survey.period.endDate)
            .bind("participantCount", survey.participantCount)
            .bind("targetType", survey.targetType.name)
            .bind("surveyType", survey.surveyType.name)
            .bind("participantType", survey.participantType.name)
            .bind("createdAt", survey.createdAt)
            .bind("updatedAt", survey.updatedAt)
            .then()
            .thenReturn(survey.id)
    }

    override fun updateSurvey(survey: Survey): Mono<SurveyId> {
        val sql = """
            UPDATE surveys SET title = :title, status = :status, start_date = :startDate, end_date = :endDate, 
            participant_count = :participantCount, target_type = :targetType, survey_type = :surveyType, 
            participant_type = :participantType, updated_at = :updatedAt WHERE id = :id
        """.trimIndent()

        return client.sql(sql)
            .bind("id", survey.id.value)
            .bind("title", survey.title.value)
            .bind("status", survey.status.name)
            .bind("startDate", survey.period.startDate)
            .bind("endDate", survey.period.endDate)
            .bind("participantCount", survey.participantCount)
            .bind("targetType", survey.targetType.name)
            .bind("surveyType", survey.surveyType.name)
            .bind("participantType", survey.participantType.name)
            .bind("updatedAt", survey.updatedAt)
            .then()
            .thenReturn(survey.id)
    }

    override fun deleteSurvey(surveyId: SurveyId): Mono<Void> {
        val sql = "DELETE FROM surveys WHERE id = :id"
        return client.sql(sql)
            .bind("id", surveyId.value)
            .then()
    }

    override fun deleteSurveys(surveyIds: List<SurveyId>): Mono<Void> {
        if (surveyIds.isEmpty()) return Mono.empty()

        return Flux.fromIterable(surveyIds)
            .flatMap { surveyId ->
                val sql = "DELETE FROM surveys WHERE id = :id"
                client.sql(sql)
                    .bind("id", surveyId.value)
                    .then()
            }
            .then()
    }

    private fun mapRowToSurvey(row: Row): Survey {
        return Survey.reconstruct(
            id = row.get("id", String::class.java) ?: "",
            title = row.get("title", String::class.java) ?: "",
            author = row.get("author", String::class.java) ?: "",
            status = row.get("status", String::class.java) ?: SurveyStatus.DRAFT.name,
            startDate = row.get("start_date", LocalDateTime::class.java) ?: LocalDateTime.now(),
            endDate = row.get("end_date", LocalDateTime::class.java) ?: LocalDateTime.now(),
            participantCount = row.get("participant_count", Number::class.java)?.toInt() ?: 0,
            targetType = row.get("target_type", String::class.java) ?: "ALL",
            surveyType = row.get("survey_type", String::class.java) ?: "SURVEY",
            participantType = row.get("participant_type", String::class.java) ?: "MEMBER",
            timeLimit = null,
            questions = emptyList(),
            createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
            updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now()
        )
    }
} 