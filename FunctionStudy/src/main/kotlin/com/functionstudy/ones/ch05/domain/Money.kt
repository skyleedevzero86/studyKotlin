package com.functionstudy.ones.ch05.domain

data class Money(val amount: Double) {
    fun sum(other: Money) = Money(this.amount + other.amount)

    override fun toString(): String = "Money($amount)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Money) return false
        return amount == other.amount
    }

    override fun hashCode(): Int {
        return amount.hashCode()
    }
}

val zeroMoney = Money(0.0)