// 루트 프로젝트는 빌드 산출물이 없는 "애그리게이터" 입니다.
// 플러그인 버전만 여기서 한 번 선언하고(apply false), 각 모듈은 버전 없이 적용합니다.
plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.springframework.boot") version "3.4.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    // 모듈 GAV 충돌 방지: bff:admin 과 service:admin 은 둘 다 이름이 "admin" 이라
    // 단일 group("com.fillsa") 에서 같은 좌표(com.fillsa:admin)로 겹쳐,
    // project(":service:admin") 의존이 :bff:admin 자기 자신으로 접혀 순환 의존이 생긴다.
    // 상위 경로(bff/service)를 group 에 반영해 좌표를 분리한다.
    group = when {
        path.startsWith(":bff") -> "com.fillsa.bff"
        path.startsWith(":service") -> "com.fillsa.service"
        else -> "com.fillsa"
    }
    version = "0.0.1-SNAPSHOT"

    // Spring Boot 3.0.7 BOM 은 kotlin-stdlib 를 1.7.22 로 관리하는데, 이 값이
    // Kotlin 1.9.25 컴파일러 클래스패스(kotlinCompilerClasspath)까지 강등시켜
    // 컴파일 데몬이 kotlin.enums.EnumEntriesKt 를 못 찾고 죽는다.
    // Kotlin 플러그인 버전(1.9.25)에 맞춰 Spring 관리 kotlin 버전을 올려 정합성을 맞춘다.
    extra["kotlin.version"] = "1.9.25"

    repositories {
        mavenCentral()
    }
}
