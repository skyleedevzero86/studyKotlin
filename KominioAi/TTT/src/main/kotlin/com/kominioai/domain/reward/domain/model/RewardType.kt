package com.kominioai.domain.reward.domain.model

enum class RewardType(val displayName: String, val description: String) {
    GIFTCARD("기프티콘", "온라인 상품권"),
    POINTS("포인트", "적립 포인트"),
    COUPON("쿠폰", "할인 쿠폰"),
    CASH("현금", "현금 보상"),
    DIGITAL("디지털 상품", "디지털 콘텐츠"),
    PHYSICAL("물리적 상품", "실물 상품")
}
