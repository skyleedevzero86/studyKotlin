package com.functionstudy.ones.ch03.utils

import com.functionstudy.ones.ch03.items.actions.DdtActions

class ScenarioBuilder<A : Any> {
    private var setupBlock: (() -> Unit)? = null
    private var playBlock: (() -> Unit)? = null
    private var thenBlock: (() -> Unit)? = null

    fun give(block: () -> Unit): ScenarioBuilder<A> {
        setupBlock = block
        return this
    }

    fun whenPlay(block: () -> Unit): ScenarioBuilder<A> {
        playBlock = block
        return this
    }

    fun then(block: () -> Unit): ScenarioBuilder<A> {
        thenBlock = block
        return this
    }

    fun build() {
        setupBlock?.invoke()
        playBlock?.invoke()
        thenBlock?.invoke()
    }
}

fun ddtScenario(block: ScenarioBuilder<DdtActions>.() -> Unit): ScenarioBuilder<DdtActions> {
    val scenario = ScenarioBuilder<DdtActions>()
    scenario.block()
    scenario.build()
    return scenario
}