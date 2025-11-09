package com.kominioai.domain.reward.domain.model

enum class RewardStatus(val displayName: String) {
    PENDING("대기중"),
    CLAIMED("수령완료"),
    EXPIRED("만료됨"),
    CANCELLED("취소됨")
}
