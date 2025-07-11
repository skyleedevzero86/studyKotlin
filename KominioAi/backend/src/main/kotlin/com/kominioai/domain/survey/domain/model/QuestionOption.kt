package com.kominioai.domain.survey.domain.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "question_options")
data class QuestionOption(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),
    val questionId: String,
    @Column(nullable = false)
    val text: String,
    @Column(name = "order_index", nullable = false)
    val orderIndex: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question? = null
) {
    protected constructor() : this(
        questionId = "",
        text = "",
        orderIndex = 0,
        question = null
    )
}