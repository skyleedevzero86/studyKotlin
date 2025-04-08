package com.functionstudy.onestest.ch01

import com.functionstudy.ch01.checkout.CashRegister
import com.functionstudy.ch01.discount.MilkDiscountStrategy
import com.functionstudy.ch01.domain.Product
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SupermarketTest {

    private val milkProduct = Product("milk", 1.5)
    private val discountStrategy = MilkDiscountStrategy()
    private val cashRegister = CashRegister(discountStrategy)

    @Test
    fun `우유 3개 구매 시 2개 가격으로 계산`() {
        // Given
        val products = listOf(milkProduct)
        val quantities = listOf(3)

        // When
        val totalPrice = cashRegister.checkout(products, quantities)

        // Then
        assertEquals(3.0, totalPrice) // 3개 구매 = 2개 가격
    }

    @Test
    fun `우유 4개 구매 시 3개 가격으로 계산`() {
        // Given
        val products = listOf(milkProduct)
        val quantities = listOf(4)

        // When
        val totalPrice = cashRegister.checkout(products, quantities)

        // Then
        assertEquals(4.5, totalPrice) // 4개 구매 = 3개 가격
    }

    @Test
    fun `우유 5개 구매 시 4개 가격으로 계산`() {
        // Given
        val products = listOf(milkProduct)
        val quantities = listOf(5)

        // When
        val totalPrice = cashRegister.checkout(products, quantities)

        // Then
        assertEquals(6.0, totalPrice) // 5개 구매 = 4개 가격
    }

    @Test
    fun `다른 상품은 정상 가격으로 계산`() {
        // Given
        val eggProduct = Product("eggs", 2.0)
        val products = listOf(eggProduct)
        val quantities = listOf(3)

        // When
        val totalPrice = cashRegister.checkout(products, quantities)

        // Then
        assertEquals(6.0, totalPrice) // 계란은 정상 가격
    }

    @Test
    fun `우유와 다른 상품 혼합 구매`() {
        // Given
        val eggProduct = Product("eggs", 2.0)
        val products = listOf(milkProduct, eggProduct)
        val quantities = listOf(3, 2)

        // When
        val totalPrice = cashRegister.checkout(products, quantities)

        // Then
        assertEquals(9.0, totalPrice) // 우유 3개(2개 가격) + 계란 2개
    }

    @Test
    fun `1싸이클 더 해보는 테스트`() {
        // Given
        val products = listOf(milkProduct)
        val quantities = listOf(6)

        // When
        val totalPrice = cashRegister.checkout(products, quantities)

        // Then
        assertEquals(6.0, totalPrice) // 6개 구매 = 4개 가격
    }
}
