package com.sleekydz86.youtubedownload.global.ytdlp

import com.sleekydz86.youtubedownload.video.SharedVideo
import java.nio.file.Path

fun ytDlpArguments(ytDlpCommand: String, video: SharedVideo, directory: Path): List<String> =
    listOf(
        ytDlpCommand,
        "--no-playlist",
        "--no-progress",
        "--windows-filenames",
        "-f", "bv*+ba/b",
        "--merge-output-format", "mp4",
        "--print", "after_move:filepath",
        "-o", directory.resolve("%(title)s [%(id)s].%(ext)s").toString(),
        video.watchUrl,
    )
