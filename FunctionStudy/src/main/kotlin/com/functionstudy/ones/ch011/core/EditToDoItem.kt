package com.functionstudy.ones.ch011.core

import java.util.*

data class EditToDoItem(val listId: UUID, val itemId: UUID, val newText: String) : Commands