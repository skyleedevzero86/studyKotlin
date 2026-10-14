package com.sleekydz86.youtubedownload.global.embedding

object DenseVectorMapping {
    fun createIndexJson(dims: Int, similarity: String, indexType: String, m: Int, efConstruction: Int): String {
        IndexOptionRules.validate(dims, indexType, similarity, m, efConstruction)
        return mappingJson(dims, similarity, indexOptionsJson(indexType, m, efConstruction))
    }

    fun reactionFieldsJson(): String = """
        {
          "properties": {
            "likeCount": { "type": "integer" },
            "dislikeCount": { "type": "integer" },
            "viewCount": { "type": "integer" }
          }
        }
    """.trimIndent()

    private fun indexOptionsJson(indexType: String, m: Int, efConstruction: Int): String {
        if (!IndexOptionRules.usesHnsw(indexType)) return """{ "type": "$indexType" }"""
        return """{ "type": "$indexType", "m": $m, "ef_construction": $efConstruction }"""
    }

    private fun mappingJson(dims: Int, similarity: String, indexOptions: String): String = """
        {
          "settings": { "index": { "mode": "vectordb_document" } },
          "mappings": {
            "_meta": {
              "study_dims": "$dims",
              "study_similarity": "$similarity",
              "study_index_mode": "vectordb_document"
            },
            "properties": {
              "id": { "type": "long" },
              "videoId": { "type": "keyword" },
              "title": { "type": "keyword" },
              "status": { "type": "keyword" },
              "bucket": { "type": "keyword" },
              "objectKey": { "type": "keyword" },
              "fileName": { "type": "keyword" },
              "sizeBytes": { "type": "long" },
              "errorMessage": { "type": "keyword", "index": false },
              "updatedAt": { "type": "date" },
              "likeCount": { "type": "integer" },
              "dislikeCount": { "type": "integer" },
              "viewCount": { "type": "integer" },
              "embedding": {
                "type": "dense_vector",
                "dims": $dims,
                "index": true,
                "similarity": "$similarity",
                "element_type": "float",
                "index_options": $indexOptions
              }
            }
          }
        }
    """.trimIndent()
}
