package com.functionstudy.ones.ch01.discount

import com.functionstudy.ch01.domain.Product

class MilkDiscountStrategy : DiscountStrategy {
    override fun calculateDiscountedPrice(product: Product, quantity: Int): Double {
        return if (product.name == "milk") {
            val fullSetPrice = (quantity / 3) * (2 * product.originalPrice)
            val remainderPrice = (quantity % 3) * product.originalPrice
            fullSetPrice + remainderPrice
        } else {
            product.originalPrice * quantity
        }
    }
}