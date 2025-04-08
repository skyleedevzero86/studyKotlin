package com.functionstudy.ones.ch03.pricing

import com.functionstudy.ch03.items.Item
object Pricing {
    private val itemPrices = mapOf(
        Item.CARROT to 2.0,
        Item.MILK to 5.0
    )

    fun getPrice(item: Item): Double = itemPrices[item] ?: 0.0
}