package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.domain.Question as DomainQuestion
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "questions")
data class Question(
    @Id
    val id: String,

    @Column(name = "survey_id", nullable = false)
    val surveyId: String,

    @Column(name = "order_num", nullable = false)
    val orderIndex: Int,

    @Column(nullable = false, length = 500)
    val text: String,

    @Column(length = 1000)
    val description: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: QuestionType,

    @Column(nullable = false)
    val required: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", insertable = false, updatable = false)
    val survey: Survey?,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val options: List<QuestionOption> = mutableListOf()
) {
    fun toDomain(): DomainQuestion {
        return DomainQuestion(
            id = QuestionId.from(id),
            surveyId = SurveyId.from(surveyId),
            order = orderIndex,
            text = text,
            description = description,
            type = type,
            required = required,
            options = options.sortedBy { it.orderIndex }.map { it.toDomain() }
        )
    }

    companion object {
        fun from(question: DomainQuestion): Question {
            return Question(
                id = question.id.value,
                surveyId = question.surveyId.value,
                orderIndex = question.order,
                text = question.text,
                description = question.description,
                type = question.type,
                required = question.required,
                survey = null
            )
        }
    }
}