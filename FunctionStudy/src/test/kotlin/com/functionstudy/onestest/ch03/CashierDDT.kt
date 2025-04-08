package com.functionstudy.onestest.ch03

import com.functionstudy.ones.ch03.cashier.Cashier
import com.functionstudy.ones.ch03.items.Item
import com.functionstudy.ones.ch03.items.actions.DdtActions
import org.junit.jupiter.api.Test
import com.functionstudy.ones.ch03.utils.*

class CashierDDT : DomainDriven<DdtActions>(allActions) {

    @DDT
    fun `고객은 아이템을 구매할 수 있다`() = ddtScenario {
        val prices = mapOf(
            Item.CARROT.name to 2.0,
            Item.MILK.name to 5.0
        )
        val cashier = Cashier()

        // give: 사전 조건 설정
        give {
            cashier.setupPrices(prices)
        }

        // when: 행동 정의
        whenPlay {
            val alice = NamedActor("Alice")
            alice.`아이템을 #qty 추가한다`(3, Item.CARROT)
            alice.`아이템을 #qty 추가한다`(1, Item.MILK)
        }

        // then: 결과 검증
        then {
            val alice = NamedActor("Alice")
            alice.`총합이 #total인지 확인`(11.0)
        }
    }

    @Test
    fun `문자열 위치 반환하기`() {
        // give: 문자열을 준비
        val myCharAtPos = buildCharAtPos("Kotlin")

        // when: 첫 번째 문자를 요청
        val result = myCharAtPos(0)

        // then: 결과가 예상한 문자 'K'인지 확인
        println("첫 번째 문자: $result")
        expectThat(result).isEqualTo('K')
    }

    @Test
    fun `템플릿 엔진 테스트`() {
        // give: 템플릿 문자열과 데이터를 준비
        val template = "Happy Birthday {name} {surname}! from {sender}."
        val data = mapOf(
            "name" to StringTag("Uberto"),
            "surname" to StringTag("Barbini"),
            "sender" to StringTag("PragProg")
        )

        // when: 템플릿을 렌더링
        val actual = renderTemplate(template, data)

        // then: 결과가 예상한 출력과 일치하는지 확인
        val expected = "Happy Birthday Uberto Barbini! from PragProg."
        println("실제 출력: $actual")
        println("예상 출력: $expected")
        expectThat(actual).isEqualTo(expected)
    }
}
