// service:admin — 어드민 계정(별도 admins 테이블) 도메인. 앱 회원(members)과 분리된 인증 주체.
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val springBootVersion = "3.0.7"

dependencies {
    implementation(project(":util"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    // 비밀번호 해시(BCrypt) 검증을 위해 security crypto 사용
    implementation("org.springframework.security:spring-security-crypto:6.0.3")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
