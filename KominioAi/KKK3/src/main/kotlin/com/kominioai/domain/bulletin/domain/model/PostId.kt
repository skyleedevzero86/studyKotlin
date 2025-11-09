package com.kominioai.domain.bulletin.domain.model

import java.util.UUID

@JvmInline
value class PostId(val value: String) {
    companion object {
        fun generate(): PostId = PostId(UUID.randomUUID().toString())
    }
}
