plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
    kotlin("plugin.jpa") version "2.3.20"
    kotlin("kapt") version "2.3.20"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.sleekydz86"
version = "0.0.1-SNAPSHOT"
description = "k_querydsl_study"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // H2 (테스트/개발용)
    runtimeOnly("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-h2console")

    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:2.0.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    mockitoAgent("org.mockito:mockito-core") {
        isTransitive = false
    }



}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// QueryDSL Q-클래스 생성 경로를 소스셋에 추가
sourceSets {
    main {
        java.srcDir("${layout.buildDirectory.get()}/generated/source/kapt/main")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
}