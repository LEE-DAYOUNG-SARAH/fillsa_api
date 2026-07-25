package com.fillsa.app.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.util.Base64

/**
 * Firebase Admin SDK 초기화 (FCM 발송용).
 * 설정(fcm.service-account-json)이 비어 있으면 초기화를 건너뛰고 발송만 비활성화된다 — 부팅에는 영향 없음.
 * 값은 서비스 계정 JSON 의 base64 또는 원문 둘 다 허용.
 */
@Configuration
class FirebaseConfig(
    @Value("\${fcm.service-account-json:}") private val serviceAccountJson: String,
) {
    private val log = KotlinLogging.logger { }

    @PostConstruct
    fun init() {
        if (serviceAccountJson.isBlank()) {
            log.warn { "FCM 서비스 계정 미설정(fcm.service-account-json) — 푸시 발송 비활성" }
            return
        }
        if (FirebaseApp.getApps().isNotEmpty()) return

        val jsonBytes = runCatching {
            Base64.getDecoder().decode(serviceAccountJson.replace("\\s".toRegex(), ""))
        }.getOrElse { serviceAccountJson.toByteArray() } // 원문 JSON 도 허용

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(ByteArrayInputStream(jsonBytes)))
            .build()
        FirebaseApp.initializeApp(options)
        log.info { "FirebaseApp 초기화 완료 — 푸시 발송 활성" }
    }
}
