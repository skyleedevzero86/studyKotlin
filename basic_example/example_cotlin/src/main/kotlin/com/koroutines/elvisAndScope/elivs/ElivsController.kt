package com.koroutines.elvisAndScope.elivs

/**
 * 간단한 스프링 구조 모방 및 실제 사용할듯한 구조 구현
 *
 * 엘비스 연산자
 * 1. ?: - 연산자는 실제로 자주 사용하고 있음, a ?= null ? a : b
 * 2. !! - 해당 연산자는 null이 아님을 보증하지만 잘못 사용하다 NPE 발생할 수 있다. 흠 언제 사용할까? 확실하게 유기적인 구조라면 사용 가능하겠지만, 그럴 일이 없는게 좋을 거 같음
 * 3. ?. - 해당 연산자가 처음에 가장 헷갈렸는데, 쉽게 `val name = member?.name` 인 경우 name이 있으면 name 반환, 없으면 null 반환
 * 4. ?. ?: - 둘을 혼합할 수 있겠다. 값을 가져오거나 혹은 없다면 예외나 기본 값을 주거나
 */

class ElivsController {

    fun execute() {
        val service = Service(InMemoryRepository())
        val member = initData(service)

        service.updateByIdOrThrow(
            id = member.id,
            newName = "jetty"
        ).let { println("${it.id}, ${it.name}") }
    }

}

fun main() {
    val controller = ElivsController()
    controller.execute()  // 결과 출력
}

private fun initData(service: Service): Member {
    val entity = Member(name = "jay")
    val savedEntity = service.save(entity)
    return savedEntity
}