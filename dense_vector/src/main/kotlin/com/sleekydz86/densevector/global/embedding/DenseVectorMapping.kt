package com.sleekydz86.densevector.global.embedding

object DenseVectorMapping {
    fun createIndexJson(dims: Int, similarity: String, indexType: String, m: Int, efConstruction: Int): String {
        IndexOptionRules.validate(dims, indexType, similarity, m, efConstruction)
        return mappingJson(dims, similarity, indexOptionsJson(indexType, m, efConstruction))
    }

    private fun indexOptionsJson(indexType: String, m: Int, efConstruction: Int): String {
        if (!IndexOptionRules.usesHnsw(indexType)) {
            return """{ "type": "$indexType" }"""
        }
        return """{ "type": "$indexType", "m": $m, "ef_construction": $efConstruction }"""
    }

    private fun mappingJson(dims: Int, similarity: String, indexOptions: String): String = """
        {
          "mappings": {
            "_meta": {
              "study_dims": "$dims",
              "study_similarity": "$similarity"
            },
            "properties": {
              "title": { "type": "keyword" },
              "body": { "type": "text" },
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
