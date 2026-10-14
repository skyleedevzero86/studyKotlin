package com.sleekydz86.youtubemsa.download.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "원장에 반영할 반응")
data class ReactionBody(
    @field:Schema(description = "LIKE, DISLIKE, VIEW", example = "LIKE")
    val reaction: String = "",
)
