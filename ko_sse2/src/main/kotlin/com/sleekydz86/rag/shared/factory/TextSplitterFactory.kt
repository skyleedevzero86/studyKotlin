package com.sleekydz86.rag.shared.factory

import com.sleekydz86.rag.shared.strategy.CustomTextSplitter
import com.sleekydz86.rag.shared.strategy.ParagraphTextSplitter
import com.sleekydz86.rag.shared.strategy.TextSplitStrategy
import org.springframework.stereotype.Component
import com.sleekydz86.rag.shared.strategy.SentenceTextSplitter

@Component
class TextSplitterFactory(
    private val customSplitter: CustomTextSplitter,
    private val sentenceSplitter: SentenceTextSplitter,
    private val paragraphSplitter: ParagraphTextSplitter
) {
    enum class SplitterType {
        CUSTOM, SENTENCE, PARAGRAPH
    }

    fun createSplitter(type: SplitterType): TextSplitStrategy = when (type) {
        SplitterType.CUSTOM -> customSplitter
        SplitterType.SENTENCE -> sentenceSplitter
        SplitterType.PARAGRAPH -> paragraphSplitter
    }
}