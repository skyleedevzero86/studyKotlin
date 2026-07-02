package com.kochat.adapter.outbound.persistence.survey

import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.survey.model.ParticipantStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "survey_participants",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["survey_id", "user_id"]),
    ],
    indexes = [
        Index(name = "idx_survey_participant_survey_id", columnList = "survey_id"),
        Index(name = "idx_survey_participant_user_id", columnList = "user_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class SurveyParticipantJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    var survey: SurveyJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ParticipantStatus = ParticipantStatus.PENDING

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var assignedAt: LocalDateTime? = null

    @Column
    var completedAt: LocalDateTime? = null
}
