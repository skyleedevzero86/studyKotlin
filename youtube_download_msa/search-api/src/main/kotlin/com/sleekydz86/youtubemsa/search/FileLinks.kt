package com.sleekydz86.youtubemsa.search

fun interface FileLinks {
    fun link(bucket: String, objectKey: String): String
}
