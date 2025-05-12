package com.functionstudy.ones.ch011.controller

// ContextReader 정의
class ContextReader<CTX, out A>(val runWith: (CTX) -> A) {
    fun <B> map(f: (A) -> B): ContextReader<CTX, B> =
        ContextReader { ctx -> f(runWith(ctx)) }
}

// 커리 연산자들 정의
infix fun <CTX, A, B> ContextReader<CTX, (A) -> B>.apply(a: ContextReader<CTX, A>): ContextReader<CTX, B> =
    ContextReader { ctx ->
        val f = this.runWith(ctx)
        val v = a.runWith(ctx)
        f(v)
    }

fun <CTX, A, B, C> ((A, B) -> C).curriedWith(a: ContextReader<CTX, A>): ContextReader<CTX, (B) -> C> =
    a.map { aVal -> { b: B -> this(aVal, b) } }

fun <CTX, A, B, C, D> ((A, B, C) -> D).curriedWith(a: ContextReader<CTX, A>): ContextReader<CTX, (B) -> (C) -> D> =
    a.map { aVal -> { b: B -> { c: C -> this(aVal, b, c) } } }

// Env 정의
data class Env(val x: Int, val y: Int, val z: Int)

// ContextReader 인스턴스들 정의
val readX = ContextReader<Env, Int> { it.x }
val readY = ContextReader<Env, Int> { it.y }
val readZ = ContextReader<Env, Int> { it.z }

// 사용할 함수: 3개의 Int를 더함
val sum3: (Int, Int, Int) -> Int = { a, b, c -> a + b + c }

// 조합
val combinedReader: ContextReader<Env, Int> =
    ((sum3.curriedWith(readX)).apply(readY)).apply(readZ)

// 컨트롤러 클래스
class ContextReaderOperatorsController {
    fun execute() {
        val env = Env(1, 2, 3)
        val result = combinedReader.runWith(env)
        println(result) // 출력: 6
    }
}

fun main() {
    val controller = ContextReaderOperatorsController()
    controller.execute()
}
