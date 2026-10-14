package com.sleekydz86.youtubemsa.media

fun mediaType(fileName: String): String =
    when (fileName.substringAfterLast('.', "").lowercase()) {
        "mp4" -> "video/mp4"
        "webm" -> "video/webm"
        "mkv" -> "video/x-matroska"
        else -> "application/octet-stream"
    }
