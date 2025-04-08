package com.functionstudy.ones.ch01.discount

import com.functionstudy.ch01.domain.Product


interface DiscountStrategy {
    fun calculateDiscountedPrice(product: Product, quantity: Int): Double
}