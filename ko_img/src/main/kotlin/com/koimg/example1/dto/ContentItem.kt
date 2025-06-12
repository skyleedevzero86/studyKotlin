package com.koimg.example1.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class ContentItem(
    val type: String,
    val text: String? = null,
    @JsonProperty("image_url") val imageUrl: ImageUrl? = null
)