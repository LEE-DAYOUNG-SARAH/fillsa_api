plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.fillsa"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

configurations {
    configureEach {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

val springBootVersion = "3.0.7"
val mysqlConnectorVersion = "8.0.33"
val kotlinVersion = "1.8.22"
val jacksonVersion = "2.14.2"
val springDocVersion = "2.0.4"
val kotlinLoggingVersion = "3.0.5"
val kotestVersion = "5.5.4"

dependencies {
    // spring
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("com.mysql:mysql-connector-j:$mysqlConnectorVersion")
    runtimeOnly("com.h2database:h2")

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:$springBootVersion")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

    // OAuth2 Client
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client:$springBootVersion")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

//    // Kotest
//    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
//    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

    // MockK
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
