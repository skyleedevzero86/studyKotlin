package com.functionstudy.ones.ch011.core

import java.time.Instant
import java.util.*

data class ToDoItemEdited(val listId: UUID, val itemId: UUID, val newText: String, val timestamp: Instant = Instant.now()) : EventToDoItemEdited