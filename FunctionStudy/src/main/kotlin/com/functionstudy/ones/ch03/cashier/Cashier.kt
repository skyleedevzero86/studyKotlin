package com.functionstudy.ones.ch03.cashier

import com.functionstudy.ch03.items.Item
import com.functionstudy.ch03.items.actions.DdtActions
import com.functionstudy.ch03.pricing.Pricing
import com.functionstudy.ch03.utils.StringTag
import com.functionstudy.ch03.utils.renderTemplate

class Cashier {
    private val cart = mutableMapOf<String, MutableList<Pair<Item, Int>>>()
    private var prices: Map<String, Double> = mapOf()
    private val actions = mutableListOf<DdtActions>()

    fun setupPrices(newPrices: Map<String, Double>) {
        prices = newPrices
    }

    fun addItem(actorName: String, qty: Int, item: Item) {
        cart.getOrPut(actorName) { mutableListOf() }.add(Pair(item, qty))
    }

    fun addAction(action: DdtActions) {
        actions.add(action)
    }

    fun processActions() {
        actions.forEach { action ->
            println("${action.actionName}: ${action.description}")
            when (action.actionName) {
                "updatePrice" -> println("가격을 업데이트합니다.")
                "addItem" -> println("아이템을 카트에 추가합니다.")
                else -> println("알 수 없는 액션입니다.")
            }
        }
    }

    fun totalForActorName(actorName: String): Double {
        return cart[actorName]?.sumOf { (item, qty) -> Pricing.getPrice(item) * qty } ?: 0.0
    }

    fun applyPromotion3x2(item: Item) {
        cart.forEach { (actorName, items) ->
            val itemIndex = items.indexOfFirst { it.first == item }
            if (itemIndex != -1) {
                val (foundItem, qty) = items[itemIndex]
                val discountQty = qty / 3
                items[itemIndex] = Pair(foundItem, qty - discountQty)
            }
        }
    }

    fun renderGreeting(name: String): String {
        val template = "생일축하해요 {name}!!! {publisher}!!!"
        val data = mapOf(
            "name" to StringTag(name),
            "publisher" to StringTag("PragProg")
        )
        return renderTemplate(template, data)
    }
}