// bff:app — 기존 모바일 앱용 API (부팅 가능한 Spring Boot 앱).
// 현재는 기존 코드 전체가 이 모듈에 들어있고, 추후 도메인 로직을 service:* 모듈로 점진 추출합니다.
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
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
val s3Version = "2.20.40"
val coroutineVersion = "1.7.3"
val jwtVersion = "0.11.5"

dependencies {
    // 공통 + 도메인 모듈
    implementation(project(":util"))
    implementation(project(":service:appVersion"))
    implementation(project(":service:quote"))
    implementation(project(":service:notice"))
    implementation(project(":service:popup"))
    implementation(project(":service:member"))

    // spring
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")

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

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis:$springBootVersion")

    // AWS SDK v2 S3 모듈
    implementation("software.amazon.awssdk:s3:$s3Version")

    // coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")

    // FCM (iOS 포함 푸시 발송 — Firebase 프로젝트에 APNs 키 등록 필요)
    implementation("com.google.firebase:firebase-admin:9.3.0")


    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

    // MockK
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.2")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
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
