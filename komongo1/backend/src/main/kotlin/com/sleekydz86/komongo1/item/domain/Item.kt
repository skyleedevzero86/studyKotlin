package com.sleekydz86.komongo1.item.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "items")
data class Item(
    @Id
    val id: String? = null,
    val name: String,
    val description: String = "",
    @Indexed
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
