package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.repository.UserSurveyRepository
import org.springframework.r2dbc.core.DatabaseClient
import io.r2dbc.spi.Row
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class UserSurveyR2dbcRepository(
    private val client: DatabaseClient
) : UserSurveyRepository {

    override fun findSurveys(
        title: String?, status: SurveyStatus?, surveyType: SurveyType?,
        start: LocalDate?, end: LocalDate?, page: Int, size: Int
    ): Flux<Survey> {
        val sql = StringBuilder("SELECT * FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any>()
        title?.let { sql.append(" AND title ILIKE :title"); params["title"] = "%$it%" }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }
        surveyType?.let { sql.append(" AND survey_type = :surveyType"); params["surveyType"] = it.name }
        start?.let { sql.append(" AND start_date >= :start"); params["start"] = it }
        end?.let { sql.append(" AND end_date <= :end"); params["end"] = it }
        sql.append(" ORDER BY id DESC LIMIT :size OFFSET :offset")
        params["size"] = size
        params["offset"] = (page - 1) * size

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row, _ -> mapRowToSurvey(row) }.all()
    }

    override fun countSurveys(
        title: String?, status: SurveyStatus?, surveyType: SurveyType?,
        start: LocalDate?, end: LocalDate?
    ): Mono<Long> {
        val sql = StringBuilder("SELECT COUNT(*) FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any>()
        title?.let { sql.append(" AND title ILIKE :title"); params["title"] = "%$it%" }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }
        surveyType?.let { sql.append(" AND survey_type = :surveyType"); params["surveyType"] = it.name }
        start?.let { sql.append(" AND start_date >= :start"); params["start"] = it }
        end?.let { sql.append(" AND end_date <= :end"); params["end"] = it }

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row, _ -> row.get(0, Number::class.java)?.toLong() }
            .one()
            .map { it ?: 0L }
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

    private fun bindParams(
        spec: DatabaseClient.GenericExecuteSpec,
        params: Map<String, Any>
    ): DatabaseClient.GenericExecuteSpec {
        var s = spec
        params.forEach { (k, v) -> s = s.bind(k, v) }
        return s
    }
}