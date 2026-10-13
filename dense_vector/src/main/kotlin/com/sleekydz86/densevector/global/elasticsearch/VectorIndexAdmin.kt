package com.sleekydz86.densevector.global.elasticsearch

interface VectorIndexAdmin {
    fun ensureIndex()
    fun describe(): IndexDescription
}
