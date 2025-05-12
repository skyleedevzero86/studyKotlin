package com.functionstudy.ones.ch011.controller

import com.functionstudy.ones.ch10.core.ContextReader

// 연산자들 정의
infix fun <CTX, A, B> ContextReader<CTX, (A) -> B>.times(
    reader: ContextReader<CTX, A>
): ContextReader<CTX, B> =
    ContextReader { ctx ->
        val f = this.runWith(ctx)
        val v = reader.runWith(ctx)
        f(v)
    }

infix fun <CTX, A, B> ((A) -> B).invokeWith(reader: ContextReader<CTX, A>): ContextReader<CTX, B> =
    ContextReader { ctx -> this(reader.runWith(ctx)) }

infix fun <CTX, A, B> ((A) -> B).not(reader: ContextReader<CTX, A>): ContextReader<CTX, B> =
    this.invokeWith(reader)

// 커리 헬퍼 함수들
fun <A, B, C, R> ((A, B, C) -> R).curried(): (A) -> (B) -> (C) -> R =
    { a -> { b -> { c -> this(a, b, c) } } }

fun <CTX, A, B, C> ((A, B) -> C).curriedWith(a: ContextReader<CTX, A>): ContextReader<CTX, (B) -> C> =
    a.map { aVal -> { b: B -> this(aVal, b) } }

fun <CTX, A, B, C, D> ((A, B, C) -> D).curriedWith(a: ContextReader<CTX, A>): ContextReader<CTX, (B) -> (C) -> D> =
    a.map { aVal -> { b: B -> { c: C -> this(aVal, b, c) } } }

// 추가 유틸리티 함수들
fun <CTX, A, B> ContextReader<CTX, A>.bind(f: (A) -> ContextReader<CTX, B>): ContextReader<CTX, B> =
    this.flatMap(f)

fun <CTX, A, B> ContextReader<CTX, A>.transform(f: (A) -> B): ContextReader<CTX, B> =
    this.map(f)

// Env 정의
data class Env(val x: Int, val y: Int, val z: Int)

// ContextReader 인스턴스들 정의
val readX = ContextReader<Env, Int> { it.x }
val readY = ContextReader<Env, Int> { it.y }
val readZ = ContextReader<Env, Int> { it.z }

// 사용할 함수: 3개의 Int를 더함
val sum3: (Int, Int, Int) -> Int = { a, b, c -> a + b + c }

// 조합 (times 연산자 사용)
val combinedReader: ContextReader<Env, Int> =
    ((sum3.curried().invokeWith(readX)) times readY) times readZ

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