package com.functionstudy.ones.ch011.core

import java.util.*

data class ToDoList(val id: UUID, val name: String, val items: List<ToDoItem>)