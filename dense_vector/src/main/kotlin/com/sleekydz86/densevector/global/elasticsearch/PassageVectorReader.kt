package com.sleekydz86.densevector.global.elasticsearch

interface PassageVectorReader {
    fun search(query: FloatArray, k: Int): List<VectorHit>
}
