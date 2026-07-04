// bff:admin — 필사 어드민 프론트(Vite + React + shadcn) 전용 백엔드. 부팅 가능한 Spring Boot 앱.
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
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
val springDocVersion = "2.0.4"
val jwtVersion = "0.11.5"
val jacksonVersion = "2.14.2"
val kotlinVersion = "1.8.22"
val kotlinLoggingVersion = "3.0.5"

dependencies {
    // 공통 + 어드민이 노출할 도메인 모듈
    implementation(project(":util"))
    implementation(project(":service:admin"))
    implementation(project(":service:quote"))
    implementation(project(":service:popup"))
    implementation(project(":service:notice"))
    implementation(project(":service:appVersion"))
    // 필요 시 추가: implementation(project(":service:member")) 등

    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-log4j2:$springBootVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

    // JWT (어드민 토큰) — 인증 구현 단계에서 사용
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")

    runtimeOnly("com.mysql:mysql-connector-j:8.0.33")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
