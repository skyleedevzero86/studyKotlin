plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "ko_sse2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Kotlin 관련
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Spring AI 의존성들 (Spring Cloud Function 제외)
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter") {
        exclude(group = "org.springframework.cloud", module = "spring-cloud-function-context")
    }
    implementation("org.springframework.ai:spring-ai-starter-vector-store-redis") {
        exclude(group = "org.springframework.cloud", module = "spring-cloud-function-context")
    }
    implementation("org.springframework.ai:spring-ai-starter-model-transformers") {
        exclude(group = "org.springframework.cloud", module = "spring-cloud-function-context")
    }
    implementation("org.springframework.ai:spring-ai-tika-document-reader") {
        exclude(group = "org.springframework.cloud", module = "spring-cloud-function-context")
    }

    // 개발 도구
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlin:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:0.8.1")
    }
}

configurations.all {
    resolutionStrategy {
        force("ch.qos.logback:logback-classic:1.5.18")
        force("ch.qos.logback:logback-core:1.5.18")
        // Spring Cloud Function 완전 제외
        exclude(group = "org.springframework.cloud", module = "spring-cloud-function-context")
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