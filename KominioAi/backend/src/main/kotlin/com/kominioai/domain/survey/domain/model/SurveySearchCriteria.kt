package com.kominioai.domain.survey.domain.model

import java.time.LocalDate

data class SurveySearchCriteria(
    val title: SurveyTitle? = null,
    val author: Author? = null,
    val status: SurveyStatus? = null,
    val surveyType: SurveyType? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val pagination: Pagination = Pagination.default()
) {
    fun hasFilters(): Boolean = 
        title != null || author != null || status != null || 
        surveyType != null || startDate != null || endDate != null

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            errors.add("시작일은 종료일보다 이전이어야 합니다.")
        }
        
        return errors
    }

    companion object {
        fun empty(): SurveySearchCriteria = SurveySearchCriteria()
        
        fun byTitle(title: String): SurveySearchCriteria = 
            SurveySearchCriteria(title = SurveyTitle(title))
        
        fun byAuthor(author: String): SurveySearchCriteria = 
            SurveySearchCriteria(author = Author(author))
        
        fun byStatus(status: SurveyStatus): SurveySearchCriteria = 
            SurveySearchCriteria(status = status)
    }
} 