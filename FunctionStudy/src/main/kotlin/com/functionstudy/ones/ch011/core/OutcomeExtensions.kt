package com.functionstudy.ones.ch011.core

import com.functionstudy.ones.ch07.domain.Outcome
import com.functionstudy.ones.ch07.inter.OutcomeError
import com.functionstudy.ones.ch07.domain.bind

fun <E : OutcomeError, S, T : S> List<Outcome<E, T>>.reduceSuccess(f: (S, T) -> T): Outcome<E, S> {
    if (isEmpty()) {
        throw IllegalArgumentException("빈 리스트는 처리할 수 없습니다.")
    }

    val first = this[0] as Outcome<E, S>

    return this.drop(1).fold(first) { acc, outcome ->
        acc.bind { a ->
            outcome.transform { b ->
                f(a, b)
            }
        }
    }
}