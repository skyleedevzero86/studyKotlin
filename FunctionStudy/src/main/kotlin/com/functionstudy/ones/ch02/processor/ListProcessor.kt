package com.functionstudy.ones.ch02.processor

class ListProcessor {
    fun <A, B, C> processListData(
        extractListData: () -> A,
        fetchListContent: (A) -> B,
        renderHtml: (B) -> C,
        createResponse: (C) -> Unit
    ) {
        val result = extractListData()
            .let { fetchListContent(it) }
            .let { renderHtml(it) }
        createResponse(result)
    }
}

fun <A, B, C> ((A) -> B).andThen(next: (B) -> C): (A) -> C {
    return { a -> next(this(a)) }
}