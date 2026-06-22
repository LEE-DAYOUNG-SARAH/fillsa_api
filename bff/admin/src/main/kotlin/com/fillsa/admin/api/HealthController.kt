package com.fillsa.admin.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 부팅 확인용 핑 엔드포인트. (skeleton)
 */
@RestController
class HealthController {

    @GetMapping("/ping")
    fun ping(): Map<String, String> = mapOf(
        "status" to "ok",
        "service" to "bff-admin",
    )
}
