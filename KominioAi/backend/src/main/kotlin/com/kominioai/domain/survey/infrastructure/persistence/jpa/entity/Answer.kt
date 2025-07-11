package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.domain.Answer as DomainAnswer
import com.kominioai.domain.survey.domain.valueobject.AnswerId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "answers")
open class Answer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    open var id: String = UUID.randomUUID().toString(),

    @Column(name = "response_id", nullable = false)
    open var responseId: String,

    @Column(name = "question_id", nullable = false)
    open var questionId: String,

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    open var questionType: com.kominioai.domain.survey.domain.valueobject.QuestionType,

    @Column(name = "text_answer", length = 2000)
    open var textAnswer: String? = null,

    @Column(name = "created_at", nullable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", insertable = false, updatable = false)
    open var surveyResponse: SurveyResponse? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "answer_options",
        joinColumns = [JoinColumn(name = "answer_id")],
        inverseJoinColumns = [JoinColumn(name = "option_id")]
    )
    open var selectedOptions: MutableList<QuestionOption> = mutableListOf()
) {
    fun toDomain(): DomainAnswer {
        return DomainAnswer(
            id = AnswerId.from(id),
            responseId = responseId,
            questionId = QuestionId.from(questionId),
            questionType = questionType,
            textAnswer = textAnswer,
            selectedOptions = selectedOptions.map { it.toDomain() },
            createdAt = createdAt
        )
    }

    companion object {
        fun from(answer: DomainAnswer): Answer {
            return Answer(
                id = answer.id.value,
                responseId = answer.responseId,
                questionId = answer.questionId.value,
                questionType = answer.questionType,
                textAnswer = answer.textAnswer,
                createdAt = answer.createdAt
            )
        }
    }
}