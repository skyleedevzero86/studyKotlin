package com.sleekydz86.rag.shared.strategy

import org.springframework.stereotype.Component

@Component
class ParagraphTextSplitter : TextSplitStrategy {
    override fun split(text: String): List<String> =
        text.split("\n\n+".toRegex())
            .filter { it.isNotBlank() }
            .map { it.trim() }
}