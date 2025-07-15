package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.dto.QuizDetailResponse
import com.kominioai.domain.survey.application.port.`in`.GetQuizDetailUseCase
import com.kominioai.domain.survey.application.port.out.QuestionPersistencePort
import com.kominioai.domain.survey.application.port.out.SurveyPersistencePort
import com.kominioai.domain.survey.application.query.QuizDetailQuery
import com.kominioai.global.exception.domain.SurveyDomainException
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QuizDetailApplicationService(
    private val surveyPersistencePort: SurveyPersistencePort,
    private val questionPersistencePort: QuestionPersistencePort
) : GetQuizDetailUseCase {

    private val logger = LoggerFactory.getLogger(QuizDetailApplicationService::class.java)

    @Cacheable(value = ["quiz-detail"], key = "#query.surveyId.value")
    override fun getQuizDetail(query: QuizDetailQuery): Mono<QuizDetailResponse> {
        return surveyPersistencePort.findById(query.surveyId)
            .switchIfEmpty(Mono.error(
                SurveyDomainException.SurveyNotFoundException(query.surveyId)
            ))
            .flatMap { survey ->
                questionPersistencePort.findBySurveyId(query.surveyId)
                    .collectList()
                    .map { questions ->
                        QuizDetailResponse.from(survey, questions, survey.getParticipationCount())
                    }
            }
            .doOnSuccess {
                logger.info("퀴즈 상세 조회 성공: surveyId={}", query.surveyId.value)
            }
            .doOnError { error ->
                logger.error("퀴즈 상세 조회 실패: surveyId={}, error={}", query.surveyId.value, error.message)
            }
    }
}