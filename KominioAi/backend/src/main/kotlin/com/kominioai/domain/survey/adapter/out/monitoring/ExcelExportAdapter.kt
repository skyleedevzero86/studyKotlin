package com.kominioai.domain.survey.adapter.out.monitoring

import com.kominioai.domain.survey.application.port.out.ExportSurveyPort
import com.kominioai.domain.survey.application.port.out.SurveyPersistencePort
import com.kominioai.domain.survey.application.dto.SurveyResult
import com.kominioai.domain.survey.domain.model.SurveyId
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExcelExportAdapter(
    private val excelExportService: ExcelExportService,
    private val surveyPersistencePort: SurveyPersistencePort
) : ExportSurveyPort {

    override fun exportSurveyResults(surveyId: SurveyId): Mono<ByteArray> {
        return surveyPersistencePort.findById(surveyId)
            .flatMap { survey ->

                val results = generateSurveyResults(survey)
                Mono.just(excelExportService.generateSurveyResultsExcel(results))
            }
    }

    private fun generateSurveyResults(survey: com.kominioai.domain.survey.domain.model.Survey): List<SurveyResult> {
        return survey.getQuestions().mapIndexed { index, question ->
            SurveyResult(
                questionOrder = index + 1,
                questionContent = question.getContent(),
                answer = "응답 데이터는 별도 저장소에서 조회 필요"
            )
        }
    }
}