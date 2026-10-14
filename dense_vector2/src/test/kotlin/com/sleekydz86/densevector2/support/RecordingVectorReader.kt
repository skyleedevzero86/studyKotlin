package com.sleekydz86.densevector2.support

import com.sleekydz86.densevector2.global.elasticsearch.PassageVectorReader
import com.sleekydz86.densevector2.global.elasticsearch.VectorHit

class RecordingVectorReader : PassageVectorReader {
    var hits: List<VectorHit> = emptyList()

    override fun search(query: FloatArray, k: Int): List<VectorHit> = hits.take(k)
}
