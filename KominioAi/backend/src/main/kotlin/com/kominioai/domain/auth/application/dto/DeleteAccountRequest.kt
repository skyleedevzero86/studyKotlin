package com.kominioai.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class DeleteAccountRequest(
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,

    @field:NotBlank(message = "삭제 확인 문구는 필수입니다.")
    val confirmation: String
)