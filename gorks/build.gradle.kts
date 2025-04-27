plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    war
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.chaorks"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springAiVersion"] = "1.0.0-M6"

dependencies {
    // 웹 MVC
    implementation("org.springframework.boot:spring-boot-starter-web")

    // WebFlux
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Thymeleaf 템플릿 엔진
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // AI 연동
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    // Kotlin Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Kotlin 표준 라이브러리
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Kotlin 테스트
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Spring Boot 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Reactor 테스트
    testImplementation("io.projectreactor:reactor-test")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    // JUnit 플랫폼 런처
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mock 라이브러리
    testImplementation("io.mockk:mockk:1.13.5")

    // Kotest
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")

    // H2 DB (개발용 인메모리 DB)
    runtimeOnly("com.h2database:h2")

    // 클래스 타입 처리용
    implementation("com.fasterxml:classmate:1.5.1")

    //Lombok
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // BCrypt 패스워드 암호화
    implementation("org.mindrot:jbcrypt:0.4")

    // 개발 도구
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // protobuf
    implementation("com.google.protobuf:protobuf-java:4.28.3")
    implementation("org.springframework.kafka:spring-kafka")

    // Kafka 클라이언트
    implementation("org.apache.kafka:kafka-clients:3.9.0")

    // Kafka용 Avro Serializer
    implementation("io.confluent:kafka-avro-serializer:7.7.1")

    // JSON 처리
    // implementation("org.springframework.boot:spring-boot-starter-json")
   // implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
    //providedRuntime("org.springframework.boot:spring-boot-starter-tomcat") //톰캣

}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}