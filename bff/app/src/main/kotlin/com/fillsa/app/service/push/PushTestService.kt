package com.fillsa.app.service.push

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.service.quote.QuoteService
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDeviceRepository
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import java.time.LocalDate

/**
 * 푸시 테스트 발송 — 인증된 회원이 "자기 디바이스"에만 즉시 발송해볼 수 있다.
 * 09시 스케줄러와 동일한 발송 경로(FCM→APNs)를 그대로 태우므로 E2E 검증용으로 충분하다.
 */
@Service
class PushTestService(
    private val pushService: PushService,
    private val quoteService: QuoteService,
    private val memberDeviceRepository: MemberDeviceRepository,
) {
    private val log = KotlinLogging.logger { }

    @Transactional
    fun sendTestPush(member: Member, deviceId: String): Boolean {
        if (!pushService.isEnabled()) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "FCM 이 설정되지 않은 서버입니다")
        }

        val device = memberDeviceRepository.findByMemberAndDeviceId(member, deviceId)
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "등록되지 않은 디바이스: $deviceId")
        val token = device.pushToken
            ?: throw BusinessException(ErrorCode.INVALID_REQUEST, "푸시 토큰이 등록되지 않은 디바이스입니다")

        val quote = runCatching { quoteService.getDailyQuote(LocalDate.now()) }.getOrNull()
        val success = pushService.sendToToken(
            title = "오늘의 문장",
            body = quote?.korQuote ?: "테스트 푸시입니다",
            token = token,
        )

        log.info { "테스트 푸시 발송: memberSeq=${member.memberSeq}, device=$deviceId, success=$success" }
        return success
    }
}
