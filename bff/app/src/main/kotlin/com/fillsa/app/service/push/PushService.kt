package com.fillsa.app.service.push

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import mu.KotlinLogging
import org.springframework.stereotype.Service

/**
 * FCM 발송 서비스. iOS 는 Firebase 프로젝트에 APNs 키가 등록돼 있으면 FCM 토큰으로 그대로 발송된다.
 */
@Service
class PushService {
    private val log = KotlinLogging.logger { }

    fun isEnabled(): Boolean = FirebaseApp.getApps().isNotEmpty()

    /**
     * 토큰 목록에 알림 발송 (500개 단위 배치).
     * @return 무효 판정 토큰 목록 (UNREGISTERED / INVALID_ARGUMENT — 호출부에서 정리)
     */
    fun sendToTokens(title: String, body: String, tokens: List<String>): List<String> {
        if (!isEnabled()) {
            log.warn { "FirebaseApp 미초기화 — 푸시 발송 스킵 (대상 ${tokens.size}건)" }
            return emptyList()
        }
        if (tokens.isEmpty()) return emptyList()

        val invalidTokens = mutableListOf<String>()
        var success = 0

        tokens.chunked(FCM_BATCH_SIZE).forEach { batch ->
            val message = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .addAllTokens(batch)
                .build()

            val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)
            success += response.successCount

            response.responses.forEachIndexed { i, r ->
                if (!r.isSuccessful) {
                    val code = r.exception?.messagingErrorCode
                    if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                        invalidTokens += batch[i]
                    }
                    log.warn { "푸시 발송 실패 [$code] token=${batch[i].take(12)}…" }
                }
            }
        }

        log.info { "푸시 발송 완료: 성공 $success / 전체 ${tokens.size}, 무효 토큰 ${invalidTokens.size}건" }
        return invalidTokens
    }

    companion object {
        private const val FCM_BATCH_SIZE = 500
    }
}
