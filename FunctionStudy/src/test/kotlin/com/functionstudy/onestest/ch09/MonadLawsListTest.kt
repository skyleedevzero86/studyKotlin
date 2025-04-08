package com.functionstudy.onestest.ch09

import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class MonadLawsListTest {

    fun double(x: Int): List<Int> = listOf(x, x * 2)
    val value = 5

    @Test
    fun `List - Left identity`() {
        val left = listOf(value).flatMap(::double)
        val right = double(value)

        expectThat(left).isEqualTo(right)
    }

    @Test
    fun `List - Right identity`() {
        val lst = listOf(1, 2, 3)
        val left = lst.flatMap { listOf(it) }

        expectThat(left).isEqualTo(lst)
    }

    @Test
    fun `List - Associativity`() {
        val f: (Int) -> List<Int> = { listOf(it, it + 1) }
        val g: (Int) -> List<Int> = { listOf(it * 2) }

        val list = listOf(value)
        val left = list.flatMap(f).flatMap(g)
        val right = list.flatMap { x -> f(x).flatMap(g) }

        expectThat(left).isEqualTo(right)
    }
}
