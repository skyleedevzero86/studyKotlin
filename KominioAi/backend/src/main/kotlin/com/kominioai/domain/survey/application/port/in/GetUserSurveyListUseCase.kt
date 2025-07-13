package com.kominioai.domain.survey.application.port.`in`

import com.kominioai.domain.survey.application.dto.UserSurveyListResponse
import com.kominioai.domain.survey.application.dto.UserSurveyListQuery
import reactor.core.publisher.Mono

interface GetUserSurveyListUseCase {
    fun getSurveyList(query: UserSurveyListQuery): Mono<UserSurveyListResponse>
}