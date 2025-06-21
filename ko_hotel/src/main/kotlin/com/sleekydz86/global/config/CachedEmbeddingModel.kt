package com.sleekydz86.global.config

import org.springframework.ai.document.Document
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.embedding.EmbeddingRequest
import org.springframework.ai.embedding.EmbeddingResponse

class CachedEmbeddingModel(
    private val embeddingModel: EmbeddingModel
) : EmbeddingModel {

    private val cache = mutableMapOf<String, FloatArray>()

    override fun embed(text: String): FloatArray {
        return cache.getOrPut(text) {
            embeddingModel.embed(text)
        }
    }

    override fun embed(texts: MutableList<String>): MutableList<FloatArray> {
        return texts.map { embed(it) }.toMutableList()
    }

    override fun embed(document: Document): FloatArray {
        val text = extractDocumentContent(document)
        return embed(text)
    }

    override fun call(request: EmbeddingRequest): EmbeddingResponse {
        return embeddingModel.call(request)
    }

    override fun dimensions(): Int {
        return embeddingModel.dimensions()
    }

    private fun extractDocumentContent(document: Document): String {
        return try {
            val clazz = document::class.java
            try {
                val contentField = clazz.getDeclaredField("content")
                contentField.isAccessible = true
                contentField.get(document)?.toString() ?: ""
            } catch (e: Exception) {
                try {
                    val textField = clazz.getDeclaredField("text")
                    textField.isAccessible = true
                    textField.get(document)?.toString() ?: ""
                } catch (e: Exception) {
                    try {
                        val getContentMethod = clazz.getMethod("getContent")
                        getContentMethod.invoke(document)?.toString() ?: ""
                    } catch (e: Exception) {
                        try {
                            val getTextMethod = clazz.getMethod("getText")
                            getTextMethod.invoke(document)?.toString() ?: ""
                        } catch (e: Exception) {
                            document.toString()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Document content 추출 실패: ${e.message}")
            document.toString()
        }
    }
}