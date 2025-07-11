package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.ResponseId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "answers")
data class Answer(
    @Id val id: String = UUID.randomUUID().toString(),
    val responseId: ResponseId,
    val questionId: String,
    val answerText: String?,
    val selectedOptionId: String?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id")
    val response: SurveyResponse? = null
)