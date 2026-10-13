package com.sleekydz86.densevector2.global.elasticsearch

interface PassageVectorReader {
    fun search(query: FloatArray, k: Int): List<VectorHit>
}
