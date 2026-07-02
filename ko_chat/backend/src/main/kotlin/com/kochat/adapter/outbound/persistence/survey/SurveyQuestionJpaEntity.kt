package com.kochat.adapter.outbound.persistence.survey

import com.kochat.domain.survey.model.QuestionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "survey_questions",
    indexes = [
        Index(name = "idx_survey_question_survey_id", columnList = "survey_id"),
    ],
)
class SurveyQuestionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    var survey: SurveyJpaEntity? = null

    @Column(nullable = false)
    var questionNo: Int = 1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var questionType: QuestionType = QuestionType.SINGLE_CHOICE

    @Column(nullable = false, length = 500)
    var questionText: String = ""
}
