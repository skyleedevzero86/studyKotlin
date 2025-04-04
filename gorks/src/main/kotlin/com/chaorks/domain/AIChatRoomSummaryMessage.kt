package com.chaorks.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
data class AIChatRoomSummaryMessage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @CreatedDate
    var createDate: LocalDateTime? = null,

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null,

    @ManyToOne
    var chatRoom: AIChatRoom? = null,

    @Column(columnDefinition = "LONGTEXT")
    var message: String? = null,

    var startMessageIndex: Int = 0,

    var endMessageIndex: Int = 0

)
