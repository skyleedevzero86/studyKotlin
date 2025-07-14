package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.*

object ParticipationDomainService {
    fun validateParticipation(
        survey: Survey,
        participant: ParticipantInfo,
        responses: List<QuestionResponse>
    ) {

        require(survey.isActive()) { "설문 참여 기간이 아닙니다." }
        if (survey.targetType == TargetType.MEMBER) {
            require(participant.authenticated) { "회원만 참여할 수 있습니다." }
        }

        survey.getQuestions().forEach { q ->
            val resp = responses.find { it.questionId == q.id }
            resp?.validate(q.type, q.isRequired())
        }
    }
}