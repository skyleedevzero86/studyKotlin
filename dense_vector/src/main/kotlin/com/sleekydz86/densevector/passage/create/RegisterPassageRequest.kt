package com.sleekydz86.densevector.passage.create

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterPassageRequest(
    @field:NotBlank(message = "제목은 비어 있을 수 없습니다.")
    @field:Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    val title: String,
    @field:NotBlank(message = "본문은 비어 있을 수 없습니다.")
    val body: String
)
