package com.chaorks.domain

import jakarta.persistence.*
import lombok.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime


@Entity
@EntityListeners(AuditingEntityListener::class)
class AIChatRoomMessage(

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
    var userMessage: String? = null,

    @Column(columnDefinition = "LONGTEXT")
    var botMessage: String? = null

)
