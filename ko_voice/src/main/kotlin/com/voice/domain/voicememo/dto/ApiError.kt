package com.voice.domain.voicememo.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiError(
    val message: String,
    val type: String? = null,
    val code: String? = null,
    val param: String? = null
)