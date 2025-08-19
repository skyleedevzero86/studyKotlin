package com.sleekydz86.rag.shared.strategy

import org.springframework.stereotype.Component

@Component
class SentenceTextSplitter : TextSplitStrategy {
    override fun split(text: String): List<String> =
        text.split("[.!?]+".toRegex())
            .filter { it.isNotBlank() }
            .map { it.trim() }
}