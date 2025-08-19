package com.sleekydz86.rag.application.service

import org.springframework.ai.document.Document
import org.springframework.core.io.Resource

interface DocumentService {
    fun loadText(resource: Resource, fileName: String): Result<Unit>
    fun doSearch(query: String): List<Document>
}