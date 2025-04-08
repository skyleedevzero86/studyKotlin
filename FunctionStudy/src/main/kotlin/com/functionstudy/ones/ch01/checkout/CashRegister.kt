package com.functionstudy.ones.ch01.checkout

import com.functionstudy.ch01.discount.DiscountStrategy
import com.functionstudy.ch01.domain.Product

class CashRegister(
    private val discountStrategy: DiscountStrategy
) {
    // 체크아웃 시 최종 가격 계산
    fun checkout(products: List<Product>, quantities: List<Int>): Double {
        require(products.size == quantities.size) { "상품과 수량의 개수가 일치해야 합니다." }

        return products.mapIndexed { index, product ->
            discountStrategy.calculateDiscountedPrice(product, quantities[index])
        }.sum()
    }
}