// util — 전 모듈 공통(BaseEntity, 공통 예외 등). 라이브러리 모듈(부팅 X).
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

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
    }
}

dependencies {
    // BaseEntity: @MappedSuperclass + JPA Auditing
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // ErrorCode / BusinessException 가 org.springframework.http.HttpStatus 사용
    implementation("org.springframework:spring-web")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
