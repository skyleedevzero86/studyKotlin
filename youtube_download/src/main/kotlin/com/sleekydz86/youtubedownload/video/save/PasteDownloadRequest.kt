package com.sleekydz86.youtubedownload.video.save

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "유튜브 공유 주소")
data class PasteDownloadRequest(
    @field:Schema(
        description = "watch, shorts, youtu.be 주소",
        example = "https://www.youtube.com/watch?v=aaaaaaaaaaa",
    )
    val url: String = "",
)
