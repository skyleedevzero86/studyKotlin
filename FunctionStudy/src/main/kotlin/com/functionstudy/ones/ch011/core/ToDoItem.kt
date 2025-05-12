package com.functionstudy.ones.ch011.core

import java.time.Instant
import java.util.*

data class ToDoItem(
    val id: UUID,
    val text: String,
    val status: ItemStatus,
    val lastModified: Instant = Instant.now()
)