package com.azure.domain.dto

data class Request(
    // 사용자 입력 질문
    var userContent: String = "",

    // 사용자 ID
    var userId: String = ""
)