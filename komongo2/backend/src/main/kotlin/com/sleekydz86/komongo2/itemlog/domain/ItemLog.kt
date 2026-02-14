package com.sleekydz86.komongo2.itemlog.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "item_log")
data class ItemLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val itemId: String,
    val action: String,
    val createdAt: Instant = Instant.now()
)
