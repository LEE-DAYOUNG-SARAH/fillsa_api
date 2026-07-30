package com.fillsa.app.service.push

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.service.quote.QuoteService
import com.fillsa.service.member.MemberDevice
import com.fillsa.service.member.MemberDeviceRepository
import java.time.LocalDate

/**
 * 일별 명언 푸시 스케줄러 — 매일 09:00 KST, 푸시 동의한 iOS 디바이스 대상.
 * (안드로이드는 앱 로컬 알림 유지 — docs/ios-apple-login-push-plan.md §6)
 * VM 1대 · 컨테이너 1개 구성이라 분산락 없이 @Scheduled 로 충분하다.
 */
@Component
class DailyQuotePushScheduler(
    private val pushService: PushService,
    private val quoteService: QuoteService,
    private val memberDeviceRepository: MemberDeviceRepository,
) {
    private val log = KotlinLogging.logger { }

    @Scheduled(cron = "\${fillsa.push.daily-cron:0 0 9 * * *}", zone = "Asia/Seoul")
    @Transactional
    fun sendDailyQuotePush() {
        if (!pushService.isEnabled()) return

        val today = LocalDate.now()
        val quote = runCatching { quoteService.getDailyQuote(today) }.getOrNull()
        if (quote == null) {
            log.warn { "오늘($today) 배정 명언 없음 — 일별 명언 푸시 스킵" }
            return
        }

        val devices = memberDeviceRepository
            .findAllByOsTypeAndActiveYnAndPushAgreedYn(MemberDevice.OsType.IOS, "Y", "Y")
            .filter { !it.pushToken.isNullOrBlank() }
        if (devices.isEmpty()) {
            log.info { "일별 명언 푸시 대상 없음 (iOS·동의·토큰 보유 기준)" }
            return
        }

        val invalidTokens = pushService.sendToTokens(
            title = PUSH_TITLE,
            body = quote.korQuote ?: PUSH_BODY_FALLBACK,
            tokens = devices.mapNotNull { it.pushToken },
        ).toSet()

        // 무효 토큰 정리 — 다음 발송부터 대상 제외
        devices.filter { it.pushToken in invalidTokens }.forEach { it.clearPushToken() }

        log.info { "일별 명언 푸시: 대상 ${devices.size}건, 무효 정리 ${invalidTokens.size}건" }
    }

    companion object {
        private const val PUSH_TITLE = "오늘의 문장" // 안드로이드 로컬 알림·위젯과 용어 통일
        private const val PUSH_BODY_FALLBACK = "오늘의 명언이 도착했어요"
    }
}
