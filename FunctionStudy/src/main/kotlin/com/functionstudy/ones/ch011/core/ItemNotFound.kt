package com.functionstudy.ones.ch011.core

import java.util.*

data class ItemNotFound(val itemId: UUID) : DomainError {
    override val msg: String
        get() = "ID가 ${itemId}인 항목을 찾을 수 없습니다"
}
