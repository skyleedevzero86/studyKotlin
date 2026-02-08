package com.sleekydz86.skkk.domain.port

interface EmbeddingPort {
    fun embed(text: String): List<Float>
    fun embed(texts: List<String>): List<List<Float>>
    fun embedQuery(query: String, task: String): List<Float>
}
