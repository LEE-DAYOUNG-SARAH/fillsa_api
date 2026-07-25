package com.fillsa.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * 필사 앱 BFF 진입점.
 * - scanBasePackages: 앱 패키지 + 모든 service 모듈(com.fillsa.service)
 * - EntityScan / EnableJpaRepositories: service 모듈의 Entity/Repository 인식
 *   (Redis 리포지토리는 com.fillsa.app 하위에서 자동 구성으로 인식된다)
 */
@SpringBootApplication(scanBasePackages = ["com.fillsa.app", "com.fillsa.service"])
@EntityScan(basePackages = ["com.fillsa.service"])
@EnableJpaRepositories(basePackages = ["com.fillsa.app", "com.fillsa.service"])
@EnableJpaAuditing
@EnableScheduling // 일별 명언 푸시 스케줄러 (DailyQuotePushScheduler)
class FillsaApiApplication

fun main(args: Array<String>) {
    runApplication<FillsaApiApplication>(*args)
}
