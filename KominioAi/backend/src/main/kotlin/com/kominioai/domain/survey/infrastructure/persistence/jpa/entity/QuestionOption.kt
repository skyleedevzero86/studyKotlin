package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.model.domain.QuestionOption as DomainQuestionOption
import com.kominioai.domain.survey.domain.valueobject.QuestionOptionId
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "question_options")
data class QuestionOption(
    @Id
    val id: String,

    @Column(name = "question_id", nullable = false)
    val questionId: String,

    @Column(name = "order_num", nullable = false)
    val orderIndex: Int,

    @Column(nullable = false, length = 200)
    val text: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    val question: Question?
) {
    fun toDomain(): DomainQuestionOption {
        return DomainQuestionOption(
            id = QuestionOptionId.from(id),
            order = orderIndex,
            text = text
        )
    }

    companion object {
        fun from(option: DomainQuestionOption): QuestionOption {
            return QuestionOption(
                id = option.id.value,
                questionId = "",
                orderIndex = option.order,
                text = option.text,
                question = null
            )
        }
    }
}