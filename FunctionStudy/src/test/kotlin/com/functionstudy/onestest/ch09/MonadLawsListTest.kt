package com.functionstudy.onestest.ch09

import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class MonadLawsListTest {

    val value = 5

    fun double(x: Int): List<Int> = listOf(x, x * 2)

    @Test
    fun `List - Left identity`() {
        // Given
        println("값 $value 를 포함한 리스트 생성")
        val left = listOf(value).flatMap(::double)
        println("왼쪽 flatMap 결과: $left")

        val right = double(value)
        println("오른쪽 double 결과: $right")

        // when&Then
        expectThat(left).isEqualTo(right)
        println("왼쪽과 오른쪽 결과가 같음: $left == $right")
    }

    @Test
    fun `List - Right identity`() {
        // Given
        val lst = listOf(1, 2, 3)
        println("리스트 $lst 생성")

        val left = lst.flatMap { listOf(it) }
        println("왼쪽 flatMap 결과: $left")

        // when&Then
        expectThat(left).isEqualTo(lst)
        println("왼쪽 flatMap 결과와 원본 리스트가 같음: $left == $lst")
    }

    @Test
    fun `List - Associativity`() {
        // Given
        val f: (Int) -> List<Int> = { listOf(it, it + 1) }
        val g: (Int) -> List<Int> = { listOf(it * 2) }

        val list = listOf(value)
        println("값 $value 를 포함한 리스트 생성")

        val left = list.flatMap(f).flatMap(g)
        println("왼쪽 flatMap(f) 후 flatMap(g) 결과: $left")

        val right = list.flatMap { x -> f(x).flatMap(g) }
        println("오른쪽 flatMap(f) 후 flatMap(g) 결과: $right")

        // when&Then
        expectThat(left).isEqualTo(right)
        println("왼쪽과 오른쪽 결과가 같음: $left == $right")
    }
}