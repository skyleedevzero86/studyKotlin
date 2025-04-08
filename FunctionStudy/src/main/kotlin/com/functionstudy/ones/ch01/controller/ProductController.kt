package com.functionstudy.ones.ch01.controller

import com.functionstudy.ones.ch01.checkout.CashRegister
import com.functionstudy.ones.ch01.discount.MilkDiscountStrategy
import com.functionstudy.ones.ch01.domain.Product

class ProductController {
    fun execute() {
        val milkProduct = Product("milk", 1.5)
        val eggProduct = Product("eggs", 2.0)
        val breadProduct = Product("bread", 0.9)

        val discountStrategy = MilkDiscountStrategy()
        val cashRegister = CashRegister(discountStrategy)


        val scenarios = listOf(
            listOf(milkProduct) to listOf(3),   // 우유 3개 구매
            listOf(milkProduct) to listOf(4),   // 우유 4개 구매
            listOf(milkProduct, eggProduct) to listOf(3, 2)  // 우유 3개, 계란 2개 구매
        )

        scenarios.forEach { (products, quantities) ->
            val totalPrice = cashRegister.checkout(products, quantities)
            println("상품: ${products.map { it.name }}, 수량: $quantities, 총 가격: $totalPrice")
        }
    }
}

fun main() {
    val controller = com.functionstudy.ones.ch01.controller.ProductController()
    controller.execute()
}
