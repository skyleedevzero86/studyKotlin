package com.functionstudy.ones.ch10.core

data class ContextReader<CTX, out T>(val runWith: (CTX) -> T) {

    fun <U> map(f: (T) -> U): ContextReader<CTX, U> =
        ContextReader { ctx -> f(runWith(ctx)) }

    fun <U> flatMap(f: (T) -> ContextReader<CTX, U>): ContextReader<CTX, U> =
        ContextReader { ctx ->
            val result = runWith(ctx)
            f(result).runWith(ctx)
        }
}

infix fun <CTX, A, B> ContextReader<CTX, (A) -> B>.times(
    reader: ContextReader<CTX, A>
): ContextReader<CTX, B> = this.flatMap { f -> reader.map(f) }

infix fun <CTX, A, B> ((A) -> B).invokeWith(reader: ContextReader<CTX, A>): ContextReader<CTX, B> =
    ContextReader { ctx -> this(reader.runWith(ctx)) }

infix fun <CTX, A, B> ((A) -> B).not(reader: ContextReader<CTX, A>): ContextReader<CTX, B> =
    this.invokeWith(reader)

fun <A, B, C, R> ((A, B, C) -> R).curried(): (A) -> (B) -> (C) -> R =
    { a -> { b -> { c -> this(a, b, c) } } }

fun <CTX, A, B> ContextReader<CTX, A>.bind(f: (A) -> ContextReader<CTX, B>): ContextReader<CTX, B> =
    this.flatMap(f)

fun <CTX, A, B> ContextReader<CTX, A>.transform(f: (A) -> B): ContextReader<CTX, B> =
    this.map(f)