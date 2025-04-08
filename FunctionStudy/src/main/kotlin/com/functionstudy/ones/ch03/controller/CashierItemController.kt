package com.functionstudy.ones.ch03.controller

import com.functionstudy.ones.ch03.cashier.Cashier
import com.functionstudy.ones.ch03.items.Item
import com.functionstudy.ones.ch03.items.actions.DdtActions


class CashierItemController {
    fun execute() {
        val cashier = Cashier()

        val updatePriceAction = DdtActions("updatePrice", "가격을 업데이트합니다.")
        val addItemAction = DdtActions("addItem", "아이템을 카트에 추가합니다.")

        cashier.addAction(updatePriceAction)
        cashier.addAction(addItemAction)

        cashier.processActions()

        val prices = mapOf("CARROT" to 2.0, "MILK" to 5.0)
        cashier.setupPrices(prices)

        cashier.addItem("Actor1", 2, Item.CARROT)
        cashier.addItem("Actor1", 1, Item.MILK)

        val total = cashier.totalForActorName("Actor1")
        println("Actor1의 총액: $total")
    }
}
fun main() {
    val controller = CashierItemController()
    controller.execute()
}