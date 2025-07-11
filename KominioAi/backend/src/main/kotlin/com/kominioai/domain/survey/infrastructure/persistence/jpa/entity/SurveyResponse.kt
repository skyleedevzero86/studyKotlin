package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.Answer
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "survey_responses")
data class SurveyResponse(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: ResponseId,
    val surveyId: SurveyId,
    @Column(name = "respondent_id")
    val respondentId: String? = null,
    @Column(name = "submitted_at", nullable = false)
    val submittedAt: LocalDateTime = LocalDateTime.now(),
    @OneToMany(mappedBy = "response", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val answers: MutableList<Answer> = mutableListOf()
) {
    fun addAnswer(answer: Answer): SurveyResponse {
        answers.add(answer)
        return this
    }
}