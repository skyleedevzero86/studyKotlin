package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.*
import com.kominioai.domain.survey.application.port.`in`.GetUserSurveyListUseCase
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.repository.UserSurveyRepository
import com.kominioai.domain.survey.domain.service.SurveySearchService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2

@Service
class UserSurveyListService(
    private val userSurveyRepository: UserSurveyRepository
) : GetUserSurveyListUseCase {

    override fun getSurveyList(query: UserSurveyListQuery): Mono<UserSurveyListResponse> {
        SurveySearchService.validateSearch(query.title, query.status, query.surveyType, query.start, query.end)

        val countMono = userSurveyRepository.countSurveys(
            query.title, query.status, query.surveyType, query.start, query.end
        )

        val surveysFlux = userSurveyRepository.findSurveys(
            query.title, query.status, query.surveyType, query.start, query.end, query.page, query.size
        ).collectList()

        return countMono.zipWith(surveysFlux)
            .map { tuple: Tuple2<Long, List<Survey>> ->
                val total = tuple.t1
                val surveys = tuple.t2
                val startNumber = total - ((query.page - 1) * query.size)
                UserSurveyListResponse(
                    totalCount = total,
                    surveys = surveys.mapIndexed { idx, s ->
                        UserSurveyListItemDto(
                            number = startNumber - idx,
                            id = s.id.value.toLongOrNull() ?: 0L,
                            title = s.getTitle().value,
                            author = s.author.name,
                            status = s.getStatus().displayName,
                            surveyType = s.surveyType.displayName,
                            period = s.getPeriodStartDate().toLocalDate().toString() + " ~ " + s.getPeriodEndDate().toLocalDate().toString(),
                            createdAt = s.createdAt.toLocalDate().toString()
                        )
                    }
                )
            }
    }
}