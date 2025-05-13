package com.functionstudy.ones.ch011.core

import java.time.Instant
import java.util.*

open class DeleteToDoItemEvent(
    open val listId: UUID,
    open val itemId: UUID,
    open val timestamp: Instant
) : Events