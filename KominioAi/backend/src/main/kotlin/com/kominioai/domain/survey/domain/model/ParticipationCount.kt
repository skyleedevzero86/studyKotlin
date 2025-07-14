package com.kominioai.domain.survey.domain.model

@JvmInline
value class ParticipationCount(val value: Int) {
    init {
        require(value >= 0) { "참여자 수는 음수일 수 없습니다." }
        require(value <= 1_000_000) { "참여자 수는 100만을 초과할 수 없습니다." }
    }

    fun increment(): ParticipationCount = ParticipationCount(value + 1)
    fun decrement(): ParticipationCount = ParticipationCount((value - 1).coerceAtLeast(0))
    fun add(count: Int): ParticipationCount = ParticipationCount((value + count).coerceAtLeast(0))
    fun isZero(): Boolean = value == 0
    fun isHigh(): Boolean = value > 1000
} 