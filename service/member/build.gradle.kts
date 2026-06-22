// service:member — member 도메인. Entity/Repository/Service. 컨트롤러 없음(라이브러리 모듈).
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
    // MemberQuote / MemberStreak 가 quote 도메인(DailyQuote)을 참조
    implementation(project(":service:quote"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
