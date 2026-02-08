package com.sleekydz86.skkk.ui.controller

import com.sleekydz86.skkk.application.SearchBlogUseCase
import com.sleekydz86.skkk.domain.port.VectorStorePort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/docs")
class DocumentController(
    private val vectorStorePort: VectorStorePort,
    private val searchBlogUseCase: SearchBlogUseCase
) {

    @GetMapping("/collections")
    fun listCollections(): List<Map<String, Any>> = vectorStorePort.listCollections()

    @PostMapping("/collections")
    fun createCollection(@RequestParam name: String): Map<String, Any> =
        vectorStorePort.createCollection(name)

    @DeleteMapping("/collections/{id}")
    fun deleteCollection(@PathVariable id: String): ResponseEntity<Void> {
        vectorStorePort.deleteCollection(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/collections/{collectionId}/documents")
    fun getDocuments(@PathVariable collectionId: String): Map<String, Any> =
        vectorStorePort.get(collectionId, listOf("documents", "metadatas"))

    @DeleteMapping("/collections/{collectionId}/documents/{docId}")
    fun deleteDocument(
        @PathVariable collectionId: String,
        @PathVariable docId: String
    ): ResponseEntity<Void> {
        vectorStorePort.delete(collectionId, listOf(docId))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/collections/{collectionId}/search")
    fun search(
        @PathVariable collectionId: String,
        @RequestParam query: String,
        @RequestParam(defaultValue = "5") topK: Int
    ): ResponseEntity<Any> =
        searchBlogUseCase.execute(query, topK.coerceIn(1, 20), collectionId)
            .fold(
                onSuccess = { ResponseEntity.ok(it) },
                onFailure = { ResponseEntity.unprocessableEntity().body(mapOf("error" to (it.message ?: "검색에 실패했습니다"))) }
            )
}
