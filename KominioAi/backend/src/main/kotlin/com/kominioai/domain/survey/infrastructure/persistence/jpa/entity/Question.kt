package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.domain.Question as DomainQuestion
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "questions")
open class Question(
    @Id
    open var id: String,

    @Column(name = "survey_id", nullable = false)
    open var surveyId: String,

    @Column(name = "order_num", nullable = false)
    open var orderIndex: Int,

    @Column(nullable = false, length = 500)
    open var text: String,

    @Column(length = 1000)
    open var description: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    open var type: QuestionType,

    @Column(nullable = false)
    open var required: Boolean,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", insertable = false, updatable = false)
    open var survey: Survey?,

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open var options: MutableList<QuestionOption> = mutableListOf()
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