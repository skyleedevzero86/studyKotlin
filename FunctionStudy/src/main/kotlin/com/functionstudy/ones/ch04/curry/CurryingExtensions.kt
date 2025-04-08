package com.functionstudy.ones.ch04.curry

fun <A, B, C> ((A, B) -> C).curry(): (A) -> (B) -> C {
    return { a -> { b -> this(a, b) } }
}

fun <A, B> ((A) -> B).plusPlus(a: A): B = this(a)