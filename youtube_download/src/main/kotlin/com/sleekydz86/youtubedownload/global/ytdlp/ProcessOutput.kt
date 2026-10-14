package com.sleekydz86.youtubedownload.global.ytdlp

data class ProcessOutput(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)
