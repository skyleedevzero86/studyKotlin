package com.kominioai.domain.bulletin.domain.model

import java.util.UUID

@JvmInline
value class CommentId(val value: String) {
    companion object {
        fun generate(): CommentId = CommentId(UUID.randomUUID().toString())
    }
}
