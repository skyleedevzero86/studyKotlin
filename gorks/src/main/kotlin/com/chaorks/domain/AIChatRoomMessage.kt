package com.chaorks.domain

import jakarta.persistence.*
import lombok.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(
    AuditingEntityListener::class
)
class AIChatRoomMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @CreatedDate
    private var createDate: LocalDateTime? = null

    @LastModifiedDate
    private var modifyDate: LocalDateTime? = null

    @ManyToOne
    private var chatRoom: AIChatRoom? = null
    private var userMessage: String? = null
    private var botMessage: String? = null
}