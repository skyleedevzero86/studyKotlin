package com.kochat.adapter.outbound.persistence.survey

import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.survey.model.SurveyStatus
import com.kochat.domain.survey.model.TargetMode
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
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "surveys",
    indexes = [
        Index(name = "idx_survey_chat_room_id", columnList = "chat_room_id"),
        Index(name = "idx_survey_status", columnList = "status"),
        Index(name = "idx_survey_created_by", columnList = "created_by"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class SurveyJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoomJpaEntity? = null

    @Column(nullable = false, length = 200)
    var title: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SurveyStatus = SurveyStatus.DRAFT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var targetMode: TargetMode = TargetMode.ALL_MEMBERS

    @Column
    var randomTargetCount: Int? = null

    @Column
    var startAt: LocalDateTime? = null

    @Column
    var endAt: LocalDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: UserJpaEntity? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
}
