package com.functionstudy.ones.ch03.utils

import com.functionstudy.ch03.items.actions.DdtActions


val allActions = listOf(
    DdtActions("setupPrice", "가격 설정"),
    DdtActions("addItem", "아이템 추가")
)

fun setupPrices(prices: Map<String, Double>) {
    println("가격 설정 완료: $prices")
}

fun setup3x2(item: String) {
    println("$item 에 대한 3x2 프로모션 설정 완료")
}

class Assertion<T>(private val actual: T) {
    fun isEqualTo(expected: T) { // 문자열인 공백 제거
        val processedActual = if (actual is String) actual.trim() else actual
        val processedExpected = if (expected is String) expected.trim() else expected

        assert(processedActual == processedExpected) {
            "Expected '$processedExpected', but got '$processedActual'"
        }
    }
}

fun <T> expectThat(actual: T): Assertion<T> = Assertion(actual)

fun buildCharAtPos(str: String): (Int) -> Char = { pos -> str[pos] }

fun renderTemplate(template: String, data: Map<String, StringTag>): String {
    var result = template
    data.forEach { (key, tag) ->
        result = result.replace("{$key}", tag.text)
    }
    return result.trim()
}