package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

class SurveyParticipation private constructor(
    val id: ParticipationId,
    val surveyId: SurveyId,
    val participant: ParticipantInfo,
    val responses: List<QuestionResponse>,
    val status: ParticipationStatus,
    val participatedAt: LocalDateTime
) {
    companion object {
        fun create(
            surveyId: SurveyId,
            participant: ParticipantInfo,
            responses: List<QuestionResponse>
        ): SurveyParticipation {
            require(responses.isNotEmpty()) { "응답 데이터가 필요합니다." }
            return SurveyParticipation(
                id = ParticipationId.generate(),
                surveyId = surveyId,
                participant = participant,
                responses = responses,
                status = ParticipationStatus.COMPLETED,
                participatedAt = LocalDateTime.now()
            )
        }

        fun reconstruct(
            id: String,
            surveyId: String,
            participant: ParticipantInfo,
            responses: List<QuestionResponse>,
            status: String,
            participatedAt: LocalDateTime
        ): SurveyParticipation {
            return SurveyParticipation(
                id = ParticipationId.fromString(id),
                surveyId = SurveyId.fromString(surveyId),
                participant = participant,
                responses = responses,
                status = ParticipationStatus.valueOf(status),
                participatedAt = participatedAt
            )
        }
    }

    fun getResponseCount(): Int = responses.size
    fun hasResponses(): Boolean = responses.isNotEmpty()
    fun isCompleted(): Boolean = status == ParticipationStatus.COMPLETED
    fun isAbandoned(): Boolean = status == ParticipationStatus.ABANDONED
    fun isInvalid(): Boolean = status == ParticipationStatus.INVALID

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (responses.isEmpty()) {
            errors.add("응답 데이터가 필요합니다.")
        }
        
        if (participant.name.isNullOrBlank() && !participant.authenticated) {
            errors.add("참여자 이름은 필수입니다.")
        }
        
        return errors
    }

    fun isValid(): Boolean = validate().isEmpty()
}