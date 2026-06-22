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
    group = "com.fillsa"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
