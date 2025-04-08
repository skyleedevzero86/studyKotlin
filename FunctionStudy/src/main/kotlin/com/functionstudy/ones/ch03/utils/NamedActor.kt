package com.functionstudy.ones.ch03.utils

import com.functionstudy.ones.ch03.cashier.Cashier
import com.functionstudy.ones.ch03.items.Item

class NamedActor(val name: String) {

    private val cashier = Cashier()
    private var total: Double = 0.0

    fun `can add #qty item`(qty: Int, item: Any) {
        println(name + "님이 " + qty + " 개의 " + item.toString() + "을(를) 추가했습니다.")
    }

    fun `check total is #total`(total: Double) {
        println(name + "님이 총액을 확인했습니다: " + total)
    }

    fun `아이템을 #qty 추가한다`(qty: Int, item: Item) {
        cashier.addItem(name, qty, item)
        total += when (item) {
            Item.CARROT -> 2.0 * qty
            Item.MILK -> 5.0 * qty
            else -> 0.0
        }
        println("$qty 개의 ${item.name}이 장바구니에 추가되었습니다.")
    }

    fun `총합이 #total인지 확인`(total: Double) {
        val actualTotal = cashier.totalForActorName(name)
        assert(actualTotal == total) {
            "총합이 $actualTotal 이(가) 아닙니다. 예상된 값은 $total 입니다."
        }
        println("총합이 정확히 $actualTotal 입니다.")
    }
}
