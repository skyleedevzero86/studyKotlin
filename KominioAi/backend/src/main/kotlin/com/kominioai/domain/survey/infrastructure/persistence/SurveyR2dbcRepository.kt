package com.kominioai.domain.survey.infrastructure.persistence

import com.kominioai.domain.survey.application.model.*
import com.kominioai.domain.survey.application.repository.SurveyRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class SurveyR2dbcRepository(
    private val client: DatabaseClient
) : SurveyRepository {

    override fun findAll(title: String?, author: String?, status: SurveyStatus?, page: Int, size: Int): Flux<Survey> {
        val sql = StringBuilder("SELECT * FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any>()
        title?.let { sql.append(" AND title LIKE :title"); params["title"] = "%$it%" }
        author?.let { sql.append(" AND author = :author"); params["author"] = it }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }
        sql.append(" ORDER BY id DESC LIMIT :size OFFSET :offset")
        params["size"] = size
        params["offset"] = (page - 1) * size

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row: io.r2dbc.spi.Row, _: Any ->
            Survey(
                id = (row.get("id", Number::class.java)?.toLong() ?: 0L),
                title = row.get("title", String::class.java) ?: "",
                author = Author(row.get("author", String::class.java) ?: ""),
                status = SurveyStatus.valueOf(row.get("status", String::class.java) ?: "PENDING"),
                createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                participantCount = row.get("participant_count", Number::class.java)?.toInt() ?: 0,
                targetType = TargetType.valueOf(row.get("target_type", String::class.java) ?: "ALL"),
                startDate = row.get("start_date", LocalDateTime::class.java),
                endDate = row.get("end_date", LocalDateTime::class.java),
                duration = row.get("duration", String::class.java) ?: ""
            )
        }.all()
    }

    override fun count(title: String?, author: String?, status: SurveyStatus?): Mono<Long> {
        val sql = StringBuilder("SELECT COUNT(*) FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any>()
        title?.let { sql.append(" AND title LIKE :title"); params["title"] = "%$it%" }
        author?.let { sql.append(" AND author = :author"); params["author"] = it }
        status?.let { sql.append(" AND status = :status"); params["status"] = it.name }

        val spec = bindParams(client.sql(sql.toString()), params)
        return spec.map { row: io.r2dbc.spi.Row, _: Any ->
            row.get(0, Number::class.java)?.toLong() ?: 0L
        }.one()
    }

    override fun findById(id: Long): Mono<Survey> {
        return client.sql("SELECT * FROM surveys WHERE id = :id")
            .bind("id", id)
            .map { row: io.r2dbc.spi.Row, _: Any ->
                Survey(
                    id = (row.get("id", Number::class.java)?.toLong() ?: 0L),
                    title = row.get("title", String::class.java) ?: "",
                    author = Author(row.get("author", String::class.java) ?: ""),
                    status = SurveyStatus.valueOf(row.get("status", String::class.java) ?: "PENDING"),
                    createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                    updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now(),
                    participantCount = row.get("participant_count", Number::class.java)?.toInt() ?: 0,
                    targetType = TargetType.valueOf(row.get("target_type", String::class.java) ?: "ALL"),
                    startDate = row.get("start_date", LocalDateTime::class.java),
                    endDate = row.get("end_date", LocalDateTime::class.java),
                    duration = row.get("duration", String::class.java) ?: ""
                )
            }.one()
    }

    override fun deleteByIds(ids: List<Long>): Mono<Void> {
        if (ids.isEmpty()) return Mono.empty()
        val inClause = ids.joinToString(",") { "?" }
        val sql = "DELETE FROM surveys WHERE id IN ($inClause)"
        var spec = client.sql(sql)
        ids.forEachIndexed { idx, id -> spec = spec.bind(idx, id) }
        return spec.then()
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