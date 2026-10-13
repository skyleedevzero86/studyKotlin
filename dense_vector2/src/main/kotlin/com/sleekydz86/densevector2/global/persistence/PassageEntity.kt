package com.sleekydz86.densevector2.global.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "passage")
class PassageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "title", nullable = false, length = 200)
    var title: String = "",
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "body", nullable = false)
    var body: String = "",
    @Column(name = "dims", nullable = false)
    var dims: Int = 0,
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "embedding_json", nullable = false)
    var embeddingJson: String = "",
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
)
