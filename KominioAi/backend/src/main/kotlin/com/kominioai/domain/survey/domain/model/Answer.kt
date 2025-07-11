package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, insertable = false, updatable = false)
    val question: Question,

    @Column(name = "text_answer", length = 2000)
    val textAnswer: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "answer_options",
        joinColumns = [JoinColumn(name = "answer_id")],
        inverseJoinColumns = [JoinColumn(name = "option_id")]
    )
    val selectedOptions: MutableList<QuestionOption> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", insertable = false, updatable = false)
    val response: SurveyResponse? = null
)