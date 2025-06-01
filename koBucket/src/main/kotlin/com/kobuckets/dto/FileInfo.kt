package com.kobuckets.dto

data class FileInfo(
    val name: String,
    val size: Long,
    val lastModified: String,
    val url: String
)