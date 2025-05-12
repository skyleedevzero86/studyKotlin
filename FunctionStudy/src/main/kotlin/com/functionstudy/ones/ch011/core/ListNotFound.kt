package com.functionstudy.ones.ch011.core

import java.util.*

data class ListNotFound(val listId: UUID) : DomainError("목록을 찾을 수 없습니다: $listId")