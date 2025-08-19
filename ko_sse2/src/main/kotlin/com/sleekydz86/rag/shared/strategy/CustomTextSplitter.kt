package com.sleekydz86.rag.shared.strategy

import org.springframework.ai.transformer.splitter.TextSplitter
import org.springframework.stereotype.Component

@Component
class CustomTextSplitter : TextSplitter(), TextSplitStrategy {

    override fun splitText(text: String): List<String> = split(text)

    override fun split(text: String): List<String> =
        text.split("\\s*\\R\\s*\\R\\S*".toRegex())
            .filter { it.isNotBlank() }
            .map { it.trim() }
}