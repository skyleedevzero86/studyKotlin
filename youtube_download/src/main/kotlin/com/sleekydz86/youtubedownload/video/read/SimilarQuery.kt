package com.sleekydz86.youtubedownload.video.read

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "벡터 검색 요청")
data class SimilarQuery(
    @field:Schema(description = "비교할 문장", example = "등대")
    val query: String = "",
    @field:Schema(description = "1 이상 20 이하. 생략하면 5", example = "5")
    val k: Int? = null,
)
