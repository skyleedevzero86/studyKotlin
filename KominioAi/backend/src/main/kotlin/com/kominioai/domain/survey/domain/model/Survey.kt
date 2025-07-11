package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "surveys")
data class Survey(
    @Id val id: SurveyId,
    val title: String,
    val description: String?,
    val status: SurveyStatus,
    val createdBy: UserId,
    val createdAt: Instant,
    val publishedAt: Instant? = null,
    val closedAt: Instant? = null,
    @OneToMany(mappedBy = "survey", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val questions: MutableList<Question> = mutableListOf()
) {
    fun publish(): Survey {
        require(status == SurveyStatus.DRAFT) { "Only draft surveys can be published" }
        require(questions.isNotEmpty()) { "Survey must have at least one question" }

        return copy(
            status = SurveyStatus.PUBLISHED,
            publishedAt = Instant.now()
        )
    }

    fun close(): Survey {
        require(status == SurveyStatus.PUBLISHED) { "Only published surveys can be closed" }

        return copy(
            status = SurveyStatus.CLOSED,
            closedAt = Instant.now()
        )
    }

    fun addQuestion(question: Question): Survey {
        require(status == SurveyStatus.DRAFT) { "Can only add questions to draft surveys" }
        questions.add(question)
        return this
    }
}