package com.functionstudy.ones.ch011.core

import java.time.Instant
import java.util.*

data class ToDoItemDeleted(
    override val listId: UUID,
    override val itemId: UUID,
    override val timestamp: Instant = Instant.now()
) : DeleteToDoItemEvent(listId, itemId, timestamp)