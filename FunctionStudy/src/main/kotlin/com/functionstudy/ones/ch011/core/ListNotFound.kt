package com.functionstudy.ones.ch011.core

import java.util.*

data class ListNotFound(val listId: UUID) : DomainError {
    override val msg: String
        get() = "ID가 ${listId}인 목록을 찾을 수 없습니다"
}
