package com.voice.domain.voicememo.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiErrorResponse(
    val error: ApiError
)