package com.kochat.adapter.outbound.persistence.survey

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    name = "survey_options",
    indexes = [
        Index(name = "idx_survey_option_question_id", columnList = "question_id"),
    ],
)
class SurveyOptionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    var question: SurveyQuestionJpaEntity? = null

    @Column(nullable = false)
    var optionNo: Int = 1

    @Column(nullable = false, length = 300)
    var optionText: String = ""
}
