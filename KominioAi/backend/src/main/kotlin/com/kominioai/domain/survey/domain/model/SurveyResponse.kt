package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "survey_responses")
data class SurveyResponse(
    @Id val id: ResponseId,
    val surveyId: SurveyId,
    val respondentId: UserId? = null, // null for anonymous responses
    val submittedAt: Instant,
    @OneToMany(mappedBy = "response", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val answers: MutableList<Answer> = mutableListOf()
) {
    fun addAnswer(answer: Answer): SurveyResponse {
        answers.add(answer)
        return this
    }
}