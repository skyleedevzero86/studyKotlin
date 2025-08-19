package com.sleekydz86.rag.shared.strategy

interface TextSplitStrategy {
    fun split(text: String): List<String>
}