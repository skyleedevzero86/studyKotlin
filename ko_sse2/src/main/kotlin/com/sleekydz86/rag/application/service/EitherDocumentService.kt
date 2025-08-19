package com.sleekydz86.rag.application.service

import com.sleekydz86.rag.common.functional.monad.Either
import org.springframework.ai.document.Document
import org.springframework.core.io.Resource


interface EitherDocumentService {
    fun loadText(resource: Resource, fileName: String): Either<String, Unit>
    fun doSearch(query: String): Either<String, List<Document>>
}