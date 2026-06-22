package com.kochat

import com.kochat.support.TestLog
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@DisplayName("애플리케이션 컨텍스트 로드 테스트 - Spring Boot 기동 역할")
class KoChatApplicationTests {

    @Test
    @DisplayName("역할: Spring 컨텍스트 - authorization 모듈이 정상 기동된다")
    fun contextLoads() {
        val name = "Spring 컨텍스트 로드"
        TestLog.start(name)

        // given
        TestLog.given("SpringBootTest", "H2 + Security + JPA 설정")

        // when
        TestLog.`when`("contextLoads", "애플리케이션 컨텍스트 초기화")

        // then
        TestLog.then("contextLoads", "예외 없이 컨텍스트 로드 완료")

        TestLog.end(name)
    }
}
