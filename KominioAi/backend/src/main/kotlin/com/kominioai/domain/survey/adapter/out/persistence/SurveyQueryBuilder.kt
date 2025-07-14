package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.domain.model.SurveySearchCriteria
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class SurveyQueryBuilder {
    
    fun buildSearchQuery(
        client: DatabaseClient,
        criteria: SurveySearchCriteria
    ): DatabaseClient.GenericExecuteSpec {
        val sqlBuilder = StringBuilder("SELECT * FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any>()

        criteria.title?.let { title ->
            sqlBuilder.append(" AND title ILIKE :title")
            params["title"] = "%${title.value}%"
        }

        criteria.author?.let { author ->
            sqlBuilder.append(" AND author = :author")
            params["author"] = author.name
        }

        criteria.status?.let { status ->
            sqlBuilder.append(" AND status = :status")
            params["status"] = status.name
        }

        criteria.surveyType?.let { surveyType ->
            sqlBuilder.append(" AND survey_type = :surveyType")
            params["surveyType"] = surveyType.name
        }

        criteria.startDate?.let { startDate ->
            sqlBuilder.append(" AND start_date >= :startDate")
            params["startDate"] = startDate.atStartOfDay()
        }

        criteria.endDate?.let { endDate ->
            sqlBuilder.append(" AND end_date <= :endDate")
            params["endDate"] = endDate.atTime(23, 59, 59)
        }

        sqlBuilder.append(" ORDER BY created_at DESC")
        sqlBuilder.append(" LIMIT :limit OFFSET :offset")
        params["limit"] = criteria.pagination.limit
        params["offset"] = criteria.pagination.offset
        
        var spec = client.sql(sqlBuilder.toString())
        params.forEach { (key, value) ->
            spec = spec.bind(key, value)
        }
        
        return spec
    }
    
    fun buildCountQuery(
        client: DatabaseClient,
        criteria: SurveySearchCriteria
    ): DatabaseClient.GenericExecuteSpec {
        val sqlBuilder = StringBuilder("SELECT COUNT(*) FROM surveys WHERE 1=1")
        val params = mutableMapOf<String, Any>()

        criteria.title?.let { title ->
            sqlBuilder.append(" AND title ILIKE :title")
            params["title"] = "%${title.value}%"
        }

        criteria.author?.let { author ->
            sqlBuilder.append(" AND author = :author")
            params["author"] = author.name
        }

        criteria.status?.let { status ->
            sqlBuilder.append(" AND status = :status")
            params["status"] = status.name
        }

        criteria.surveyType?.let { surveyType ->
            sqlBuilder.append(" AND survey_type = :surveyType")
            params["surveyType"] = surveyType.name
        }

        criteria.startDate?.let { startDate ->
            sqlBuilder.append(" AND start_date >= :startDate")
            params["startDate"] = startDate.atStartOfDay()
        }

        criteria.endDate?.let { endDate ->
            sqlBuilder.append(" AND end_date <= :endDate")
            params["endDate"] = endDate.atTime(23, 59, 59)
        }
        
        var spec = client.sql(sqlBuilder.toString())
        params.forEach { (key, value) ->
            spec = spec.bind(key, value)
        }
        
        return spec
    }
} 