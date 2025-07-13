package com.kominioai.domain.survey.adapter.`in`.web

import com.kominioai.domain.survey.adapter.`in`.web.dto.SurveyListResponse
import com.kominioai.domain.survey.application.dto.CreateSurveyCommand
import com.kominioai.domain.survey.application.dto.SurveyDetailResponse
import com.kominioai.domain.survey.application.dto.UpdateSurveyCommand
import com.kominioai.domain.survey.application.port.`in`.GetSurveyDetailUseCase
import com.kominioai.domain.survey.application.query.SurveyDetailQuery
import com.kominioai.domain.survey.application.service.SurveyApplicationService
import com.kominioai.domain.survey.domain.model.SurveyStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/surveys")
class SurveyController(
    private val surveyService: SurveyApplicationService,
    private val getSurveyDetailUseCase: GetSurveyDetailUseCase
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) author: String?,
        @RequestParam(required = false) status: SurveyStatus?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Mono<SurveyListResponse> =
        surveyService.getSurveyList(title, author, status, page, size)
            .map { SurveyListResponse.from(it) }

    @PostMapping
    fun create(@RequestBody command: CreateSurveyCommand): Mono<Long> =
        surveyService.createSurvey(command)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody command: UpdateSurveyCommand): Mono<Long> =
        surveyService.updateSurvey(command.copy(id = id))

    @DeleteMapping
    fun delete(@RequestBody ids: List<Long>): Mono<Void> =
        surveyService.deleteSurveys(ids)

    @GetMapping("/{id}/export", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun export(@PathVariable id: Long): Mono<ByteArray> =
        surveyService.exportSurveyResults(id)

    @GetMapping("/{surveyId}/detail")
    fun getSurveyDetail(
        @PathVariable surveyId: Long,
        @RequestParam userId: String
    ): Mono<SurveyDetailResponse> {
        val query = SurveyDetailQuery(surveyId, userId)
        return getSurveyDetailUseCase.getSurveyDetail(query)
    }
}