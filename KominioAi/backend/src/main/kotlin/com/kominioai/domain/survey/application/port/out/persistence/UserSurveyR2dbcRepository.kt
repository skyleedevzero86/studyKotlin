package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.domain.model.*
import com.kominioai.domain.survey.domain.repository.UserSurveyRepository
import org.springframework.r2dbc.core.DatabaseClient
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
        start?.let { sql.append(" AND start_date >= :start"); params["start"] = start }
        end?.let { sql.append(" AND end_date <= :end"); params["end"] = end }
        sql.append(" ORDER BY id DESC LIMIT :size OFFSET :offset")
        params["size"] = size
        params["offset"] = (page - 1) * size

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row, _ ->
            val startDate = row.get("start_date", LocalDateTime::class.java)
            val endDate = row.get("end_date", LocalDateTime::class.java)

            Survey(
                id = row.get("id", Number::class.java)?.toLong(),
                title = row.get("title", String::class.java) ?: "",
                author = Author(row.get("author", String::class.java) ?: ""),
                status = SurveyStatus.fromDb(row.get("status", String::class.java) ?: "WAITING"),
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

    override fun countSurveys(
        title: String?, status: SurveyStatus?, surveyType: SurveyType?,
        start: LocalDate?, end: LocalDate?
    ): Mono<Long> {
        val sql = StringBuilder("SELECT COUNT(*) FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any>()
        title?.let { sql.append(" AND title ILIKE :title"); params["title"] = "%$it%" }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }
        surveyType?.let { sql.append(" AND survey_type = :surveyType"); params["surveyType"] = it.name }
        start?.let { sql.append(" AND start_date >= :start"); params["start"] = start }
        end?.let { sql.append(" AND end_date <= :end"); params["end"] = end }

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row, _ -> row.get(0, Number::class.java)?.toLong() }
            .one()
            .map { it ?: 0L }
    }

    private fun bindParams(
        spec: org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec,
        params: Map<String, Any>
    ): org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec {
        var s = spec
        params.forEach { (k, v) -> s = s.bind(k, v) }
        return s
    }
}