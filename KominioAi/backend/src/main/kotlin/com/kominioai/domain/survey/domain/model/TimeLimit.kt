package com.kominioai.domain.survey.domain.model

data class TimeLimit(
    val enabled: Boolean,
    val minutes: Int?
) {
    init {
        if (enabled) {
            require(minutes != null && minutes > 0) { "시간 제한이 활성화된 경우 분 단위를 지정해야 합니다." }
        }
    }
}