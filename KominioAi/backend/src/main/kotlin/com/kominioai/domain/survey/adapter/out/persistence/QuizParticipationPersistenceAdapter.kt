package com.kominioai.domain.survey.adapter.out.persistence

import com.kominioai.domain.survey.application.port.out.QuizParticipationPersistencePort
import com.kominioai.domain.survey.domain.model.ParticipationId
import com.kominioai.domain.survey.domain.model.QuizParticipation
import com.kominioai.domain.survey.domain.model.SurveyId
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
class QuizParticipationPersistenceAdapter(
    private val quizParticipationR2dbcRepository: QuizParticipationR2dbcRepository,
    private val quizAnswerR2dbcRepository: QuizAnswerR2dbcRepository
) : QuizParticipationPersistencePort {

    override fun save(participation: QuizParticipation): Mono<QuizParticipation> {
        val participationEntity = QuizParticipationEntity.fromDomain(participation)

        return quizParticipationR2dbcRepository.save(participationEntity)
            .flatMap { savedParticipation ->
                val answerEntities = participation.getAnswers().map { answer ->
                    QuizAnswerEntity.fromDomain(answer, savedParticipation.id!!)
                }

                if (answerEntities.isNotEmpty()) {
                    quizAnswerR2dbcRepository.saveAll(answerEntities)
                        .collectList()
                        .thenReturn(savedParticipation)
                } else {
                    Mono.just(savedParticipation)
                }
            }
            .map { it.toDomain() }
    }

    override fun findById(id: ParticipationId): Mono<QuizParticipation> {
        return quizParticipationR2dbcRepository.findById(id.value)
            .flatMap { participationEntity ->
                quizAnswerR2dbcRepository.findByParticipationId(participationEntity.id!!)
                    .collectList()
                    .map { answerEntities ->
                        val participation = participationEntity.toDomain()
                        val answers = answerEntities.map { it.toDomain() }

                        answers.forEach { answer ->
                            participation.addAnswer(answer)
                        }

                        participation
                    }
            }
    }

    override fun findBySurveyId(surveyId: SurveyId): Mono<List<QuizParticipation>> {
        return quizParticipationR2dbcRepository.findBySurveyId(surveyId.value)
            .collectList()
            .flatMap { participationEntities ->
                val participations = participationEntities.map { it.toDomain() }

                val participationIds = participationEntities.mapNotNull { it.id }

                if (participationIds.isNotEmpty()) {
                    quizAnswerR2dbcRepository.findByParticipationIdIn(participationIds)
                        .collectList()
                        .map { allAnswers ->
                            val answersByParticipation = allAnswers.groupBy { it.participationId }

                            participations.forEach { participation ->
                                val answers = answersByParticipation[participation.id.value]?.map { it.toDomain() } ?: emptyList()
                                answers.forEach { answer ->
                                    participation.addAnswer(answer)
                                }
                            }

                            participations
                        }
                } else {
                    Mono.just(participations)
                }
            }
    }

    override fun findBySurveyIdAndParticipantPhone(surveyId: SurveyId, phone: String): Mono<QuizParticipation?> {
        return quizParticipationR2dbcRepository.findBySurveyIdAndParticipantPhone(surveyId.value, phone)
            .flatMap { participationEntity ->
                if (participationEntity != null) {
                    findById(ParticipationId.fromString(participationEntity.id!!))
                        .map { it }
                } else {
                    Mono.empty()
                }
            }
    }

    override fun deleteById(id: ParticipationId): Mono<Boolean> {
        return quizAnswerR2dbcRepository.deleteByParticipationId(id.value)
            .then(quizParticipationR2dbcRepository.deleteById(id.value))
            .map { true }
            .onErrorReturn(false)
    }
}