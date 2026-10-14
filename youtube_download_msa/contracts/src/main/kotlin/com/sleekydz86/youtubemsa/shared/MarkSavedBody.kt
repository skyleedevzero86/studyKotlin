package com.sleekydz86.youtubemsa.shared

data class MarkSavedBody(
    val bucket: String = "",
    val objectKey: String = "",
    val fileName: String = "",
    val sizeBytes: Long = 0,
    val title: String = "",
)
