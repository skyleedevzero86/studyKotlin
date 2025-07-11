package com.kominioai.domain.survey.domain.model

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "question_options")
data class QuestionOption(
    @Id val id: String = UUID.randomUUID().toString(),
    val questionId: String,
    val text: String,
    val orderIndex: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    val question: Question? = null
)