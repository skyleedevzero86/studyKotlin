plugins {
    kotlin("jvm") version "2.4.21" apply false
    kotlin("plugin.spring") version "2.4.21" apply false
    kotlin("plugin.jpa") version "2.4.21" apply false
    id("org.springframework.boot") version "4.1.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.sleekydz86"
    version = "0.0.1-SNAPSHOT"
    extra["kotlin.version"] = "2.4.21"
    repositories {
        mavenCentral()
    }
}
