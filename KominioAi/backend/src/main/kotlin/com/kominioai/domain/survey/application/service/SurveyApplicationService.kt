package com.kominioai.domain.survey.application.service


import com.kominioai.domain.survey.application.model.Survey
import com.kominioai.domain.survey.application.model.SurveyStatus
import com.kominioai.domain.survey.application.repository.SurveyRepository
import com.kominioai.domain.survey.domain.dto.SurveyListResult
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

@Service
class SurveyApplicationService(
    private val surveyRepository: SurveyRepository
) {
    fun getSurveyList(
        title: String?,
        author: String?,
        status: SurveyStatus?,
        page: Int,
        size: Int
    ): Mono<SurveyListResult> {
        return surveyRepository.count(title, author, status)
            .zipWith(
                surveyRepository.findAll(title, author, status, page, size).collectList()
            )
            .map { tuple: Tuple2<Long, List<Survey>> ->
                SurveyListResult(
                    total = tuple.t1,
                    surveys = tuple.t2
                )
            }
    }

    fun deleteSurveys(ids: List<Long>): Mono<Void> {
        return surveyRepository.deleteByIds(ids)
    }
}
