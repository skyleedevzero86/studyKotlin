package com.sleekydz86.densevector2.global.elasticsearch

interface VectorIndexAdmin {
    fun ensureIndex()
    fun describe(): IndexDescription
}
