package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "questions")
data class Question(
    @Id val id: String = UUID.randomUUID().toString(),
    val surveyId: SurveyId,
    val title: String,
    val type: QuestionType,
    val isRequired: Boolean = false,
    val orderIndex: Int,
    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val options: MutableList<QuestionOption> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    val survey: Survey? = null
) {
    fun addOption(option: QuestionOption): Question {
        require(type.supportsOptions()) { "Question type ${type} does not support options" }
        options.add(option)
        return this
    }
}