package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.domain.Answer as DomainAnswer
import com.kominioai.domain.survey.domain.valueobject.AnswerId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "answers")
data class Answer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "response_id", nullable = false)
    val responseId: String,

    @Column(name = "question_id", nullable = false)
    val questionId: String,

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val questionType: com.kominioai.domain.survey.domain.valueobject.QuestionType,

    @Column(name = "text_answer", length = 2000)
    val textAnswer: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "answer_options",
        joinColumns = [JoinColumn(name = "answer_id")],
        inverseJoinColumns = [JoinColumn(name = "option_id")]
    )
    val selectedOptions: MutableList<QuestionOption> = mutableListOf()
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