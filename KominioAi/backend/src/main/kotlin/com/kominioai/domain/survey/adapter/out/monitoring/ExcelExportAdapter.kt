package com.kominioai.domain.survey.adapter.out.monitoring

import com.kominioai.domain.survey.application.port.out.ExportSurveyPort
import com.kominioai.domain.survey.domain.model.SurveyId
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ExcelExportAdapter(
    private val excelExportService: ExcelExportService,
    private val loadSurveyPort: LoadSurveyPort
) : ExportSurveyPort {

    override fun exportSurveyResults(surveyId: SurveyId): Mono<ByteArray> {
        return loadSurveyPort.loadSurvey(surveyId)
            .flatMap { survey ->
                // 설문결과 엑셀 추가 예정
                val results = emptyList<com.kominioai.domain.survey.application.dto.SurveyResult>()
                Mono.just(excelExportService.generateSurveyResultsExcel(results))
            }
    }
} 