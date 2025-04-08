package com.functionstudy.ones.ch04.extensions

fun <T> T.discardUnless(predicate: (T) -> Boolean): T? {
    return if (predicate(this)) this else null
}

fun <A, B, C> ((A) -> B).andUnlessNull(other: (B) -> C?): ((A) -> C?)? {
    return { a ->
        val firstResult = this(a)
        other(firstResult)
    }
}
