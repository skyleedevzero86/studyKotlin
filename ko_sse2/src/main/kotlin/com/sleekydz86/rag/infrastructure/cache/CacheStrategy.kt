package com.sleekydz86.rag.infrastructure.cache

interface CacheStrategy<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V)
    fun remove(key: K)
    fun clear()
}