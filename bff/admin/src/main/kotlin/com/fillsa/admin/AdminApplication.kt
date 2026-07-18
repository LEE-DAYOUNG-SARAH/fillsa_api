package com.fillsa.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.util.TimeZone

/**
 * 필사 어드민 BFF 진입점.
 * - scanBasePackages: 어드민 앱 패키지 + 모든 service 모듈(com.fillsa.service)
 * - EntityScan / EnableJpaRepositories: service 모듈의 Entity/Repository 인식
 */
@SpringBootApplication(scanBasePackages = ["com.fillsa.admin", "com.fillsa.service"])
@EntityScan(basePackages = ["com.fillsa.service"])
@EnableJpaRepositories(basePackages = ["com.fillsa.admin", "com.fillsa.service"])
class AdminApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    runApplication<AdminApplication>(*args)
}
