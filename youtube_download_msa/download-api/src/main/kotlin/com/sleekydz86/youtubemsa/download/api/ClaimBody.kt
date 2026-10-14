package com.sleekydz86.youtubemsa.download.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "저장을 맡길 때 쓰는 이벤트 번호")
data class ClaimBody(
    @field:Schema(description = "중복 저장을 가리는 이벤트 번호", example = "download-4")
    val eventId: String = "",
)
