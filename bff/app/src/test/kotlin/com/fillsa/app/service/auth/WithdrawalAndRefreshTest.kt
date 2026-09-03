package com.fillsa.app.service.auth

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import com.fillsa.util.exception.BusinessException
import com.fillsa.app.api.auth.LoginRequest
import com.fillsa.app.api.auth.LogoutRequest
import com.fillsa.app.api.auth.TokenRefreshRequest
import com.fillsa.app.fixture.member.persist.MemberPersistFactory
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberDevice
import com.fillsa.service.member.MemberDeviceRepository
import com.fillsa.service.member.MemberRepository

/**
 * 탈퇴·로그아웃 관련 회귀 방지.
 *
 * 세 가지 결함이 조용히 동작하고 있었다.
 *  ① 탈퇴해도 디바이스가 활성 상태로 남아 푸시가 계속 발송됨
 *  ② 로그아웃해도 리프레시 토큰이 유효기간(90일) 내내 재발급에 사용 가능
 *  ③ 웹 탈퇴 콜백에서 code 누락 시 500, e.message 가 null 이면 catch 안에서 NPE
 *
 * 셋 다 사용자에게는 정상으로 보여 발견이 늦었다. 현재 동작을 고정한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WithdrawalAndRefreshTest @Autowired constructor(
    private val sut: AuthService,
    private val memberPersistFactory: MemberPersistFactory,
    private val memberRepository: MemberRepository,
    private val memberDeviceRepository: MemberDeviceRepository,
) {

    private fun loginData(oauthId: String, deviceId: String) = LoginRequest.LoginData(
        userData = LoginRequest.UserData(
            oAuthProvider = Member.OAuthProvider.KAKAO,
            oAuthId = oauthId,
            nickname = "tester",
            profileImageUrl = null
        ),
        deviceData = LoginRequest.DeviceData(
            deviceId = deviceId,
            osType = MemberDevice.OsType.IOS,
            appVersion = "1.0.0",
            osVersion = "iOS 18",
            deviceModel = "iPhone",
            pushToken = "push-token-abc",
            pushAgreed = true
        )
    )

    // ── ① 탈퇴 시 디바이스 정리 ──────────────────────────────

    @Test
    fun `탈퇴하면 디바이스가 비활성화되고 푸시 토큰이 제거된다`() {
        // given
        val oauthId = "wd-${System.nanoTime()}"
        val (member, _) = sut.login(loginData(oauthId, "device-1"))

        val before = memberDeviceRepository.findAllByMember(member).single()
        assertThat(before.activeYn).isEqualTo("Y")
        assertThat(before.pushToken).isNotNull()

        // when
        sut.withdrawByApp(member)

        // then — 이 처리가 없으면 탈퇴자에게 푸시가 계속 발송된다
        val after = memberDeviceRepository.findAllByMember(member).single()
        assertThat(after.activeYn).isEqualTo("N")
        assertThat(after.pushToken).isNull()
    }

    @Test
    fun `탈퇴하면 회원이 탈퇴 상태가 된다`() {
        val oauthId = "wd-${System.nanoTime()}"
        val (member, _) = sut.login(loginData(oauthId, "device-1"))

        sut.withdrawByApp(member)

        assertThat(memberRepository.findById(member.memberSeq).get().isWithdrawal()).isTrue()
    }

    @Test
    fun `이미 탈퇴한 회원을 다시 탈퇴시켜도 withdrawalAt 이 덮어써지지 않는다`() {
        val oauthId = "wd-${System.nanoTime()}"
        val (member, _) = sut.login(loginData(oauthId, "device-1"))
        sut.withdrawByApp(member)
        val firstWithdrawnAt = memberRepository.findById(member.memberSeq).get().withdrawalAt

        sut.withdrawByApp(member)

        assertThat(memberRepository.findById(member.memberSeq).get().withdrawalAt)
            .isEqualTo(firstWithdrawnAt)
    }

    // ── ② 로그아웃 후 리프레시 토큰 무효화 ────────────────────

    @Test
    fun `로그아웃하면 리프레시 토큰으로 재발급할 수 없다`() {
        // given
        val oauthId = "rf-${System.nanoTime()}"
        val deviceId = "device-logout"
        val (member, login) = sut.login(loginData(oauthId, deviceId))

        // 로그아웃 전에는 재발급이 된다
        assertThat(sut.refreshToken(TokenRefreshRequest(login.refreshToken, deviceId)).accessToken)
            .isNotBlank()

        // when
        sut.logout(member, LogoutRequest(deviceId))

        // then — 검증이 없으면 서명·만료만 통과해 90일간 재발급이 계속 가능했다
        assertThatThrownBy {
            sut.refreshToken(TokenRefreshRequest(login.refreshToken, deviceId))
        }.isInstanceOf(BusinessException::class.java)
    }

    @Test
    fun `로그인 이력이 없는 디바이스로는 재발급할 수 없다`() {
        // given — device-A 로만 로그인한다
        val oauthId = "rf-${System.nanoTime()}"
        val (_, login) = sut.login(loginData(oauthId, "device-A"))

        // when & then — Redis 에 (회원, device-B) 항목이 없으므로 거부되어야 한다.
        // 검증이 없던 시절에는 서명·만료만 통과하면 어떤 deviceId 로도 재발급이 됐다.
        assertThatThrownBy {
            sut.refreshToken(TokenRefreshRequest(login.refreshToken, "device-B"))
        }.isInstanceOf(BusinessException::class.java)
    }

    @Test
    fun `탈퇴하면 리프레시 토큰으로 재발급할 수 없다`() {
        val oauthId = "rf-${System.nanoTime()}"
        val deviceId = "device-withdraw"
        val (member, login) = sut.login(loginData(oauthId, deviceId))

        sut.withdrawByApp(member)

        assertThatThrownBy {
            sut.refreshToken(TokenRefreshRequest(login.refreshToken, deviceId))
        }.isInstanceOf(BusinessException::class.java)
    }
}
