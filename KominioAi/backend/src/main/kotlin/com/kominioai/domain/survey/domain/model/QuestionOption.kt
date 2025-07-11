package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.QuestionOptionId
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "question_options")
data class QuestionOption(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "question_id", nullable = false)
    val questionId: String,

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int,

    @Column(nullable = false, length = 200)
    val text: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    val question: Question? = null
) {
    fun toDomain(): com.kominioai.domain.survey.domain.model.domain.QuestionOption {
        return com.kominioai.domain.survey.domain.model.domain.QuestionOption(
            id = QuestionOptionId.from(id),
            order = orderIndex,
            text = text
        )
    }

    companion object {
        fun from(domainOption: com.kominioai.domain.survey.domain.model.domain.QuestionOption, questionId: String): QuestionOption {
            return QuestionOption(
                id = domainOption.id.value,
                questionId = questionId,
                orderIndex = domainOption.order,
                text = domainOption.text
            )
        }
    }
}