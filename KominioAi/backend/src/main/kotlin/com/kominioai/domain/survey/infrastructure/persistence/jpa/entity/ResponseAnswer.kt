package com.kominioai.domain.survey.infrastructure.persistence.jpa.entity

import com.kominioai.domain.survey.domain.valueobject.AnswerId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "response_answers")
data class ResponseAnswer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "survey_response_id", nullable = false)
    val surveyResponseId: String,

    @Column(name = "question_id", nullable = false)
    val questionId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    val questionType: QuestionType,

    @Column(name = "text_answer", length = 2000)
    val textAnswer: String? = null,

    @Column(name = "selected_option_ids", length = 1000)
    val selectedOptionIds: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", insertable = false, updatable = false)
    var surveyResponse: SurveyResponse? = null
) {
    protected constructor() : this(
        surveyResponseId = "",
        questionId = "",
        questionType = QuestionType.SINGLE_CHOICE
    )

    fun toDomain(): com.kominioai.domain.survey.domain.model.domain.Answer {
        return com.kominioai.domain.survey.domain.model.domain.Answer(
            id = AnswerId.from(id),
            questionId = QuestionId.from(questionId),
            questionType = questionType,
            textAnswer = textAnswer,
            selectedOptions = parseSelectedOptions(),
            responseId = surveyResponseId,
            createdAt = createdAt
        )
    }

    private fun parseSelectedOptions(): List<com.kominioai.domain.survey.domain.model.domain.QuestionOption> {
        return emptyList()
    }

    companion object {
        fun from(domainAnswer: com.kominioai.domain.survey.domain.model.domain.Answer, surveyResponseId: String): ResponseAnswer {
            return ResponseAnswer(
                id = domainAnswer.id.value,
                surveyResponseId = surveyResponseId,
                questionId = domainAnswer.questionId.value,
                questionType = domainAnswer.questionType,
                textAnswer = domainAnswer.textAnswer,
                selectedOptionIds = serializeSelectedOptions(domainAnswer.selectedOptions),
                createdAt = domainAnswer.createdAt
            )
        }

        private fun serializeSelectedOptions(options: List<com.kominioai.domain.survey.domain.model.domain.QuestionOption>): String? {
            return if (options.isEmpty()) null else options.joinToString(",") { it.id.value }
        }
    }
}