package com.sleekydz86.skkk.domain.port


interface VectorStorePort {
    fun getOrCreateCollectionId(name: String): String
    fun add(
        collectionId: String,
        ids: List<String>,
        embeddings: List<List<Float>>,
        documents: List<String>,
        metadatas: List<Map<String, Any>>?
    )
    fun query(
        collectionId: String,
        queryEmbedding: List<Float>,
        nResults: Int,
        include: List<String>
    ): Map<String, Any>

    fun listCollections(): List<Map<String, Any>>
    fun createCollection(name: String): Map<String, Any>
    fun deleteCollection(collectionId: String)
    fun get(collectionId: String, include: List<String>): Map<String, Any>
    fun delete(collectionId: String, ids: List<String>)
}
