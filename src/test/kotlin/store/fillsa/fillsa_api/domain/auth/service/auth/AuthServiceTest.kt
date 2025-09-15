package store.fillsa.fillsa_api.domain.auth.service.auth

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.auth.dto.LogoutRequest
import store.fillsa.fillsa_api.domain.auth.dto.TokenRefreshRequest
import store.fillsa.fillsa_api.domain.auth.service.AuthService
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.entity.MemberDevice
import store.fillsa.fillsa_api.domain.members.member.service.MemberService
import store.fillsa.fillsa_api.fixture.member.persist.MemberPersistFactory

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest @Autowired constructor(
    private val sut: AuthService,
    private val memberService: MemberService,
    private val memberPersistFactory: MemberPersistFactory
) {
    
    @Test
    fun `로그인 성공 - 새로운 토큰을 생성하고 반환한다`() {
        // given
        val loginData = LoginRequest.LoginData(
            userData = LoginRequest.UserData(
                oAuthProvider = Member.OAuthProvider.GOOGLE,
                oAuthId = "test-oauth-id-" + System.currentTimeMillis(),
                nickname = "testNick",
                profileImageUrl = "testUrl"
            ),
            deviceData = LoginRequest.DeviceData(
                deviceId = "test-device-id",
                osType = MemberDevice.OsType.ANDROID,
                appVersion = "1.0.0",
                osVersion = "10.0.0",
                deviceModel = "Test Device"
            )
        )
        
        // when
        val result = sut.login(loginData)
        
        // then
        assertThat(result.first).isNotNull
        assertThat(result.first.oauthId).startsWith(loginData.userData.oAuthId)
        assertThat(result.first.nickname).isEqualTo(loginData.userData.nickname)
        assertThat(result.second.accessToken).isNotBlank
        assertThat(result.second.refreshToken).isNotBlank
    }
    
    @Test
    fun `로그아웃 성공 - 정상적으로 로그아웃 처리된다`() {
        // given
        val member = memberPersistFactory.createMember()
        val logoutRequest = LogoutRequest(deviceId = "test-device-id")
        
        // when & then
        assertThatCode { sut.logout(member, logoutRequest) }.doesNotThrowAnyException()
    }
    
    @Test
    fun `토큰 재발급 성공 - 새로운 토큰을 생성하고 반환한다`() {
        // given
        val loginData = LoginRequest.LoginData(
            userData = LoginRequest.UserData(
                oAuthProvider = Member.OAuthProvider.KAKAO,
                oAuthId = "refresh-test-oauth-id-" + System.currentTimeMillis(),
                nickname = "refreshTestNick",
                profileImageUrl = null
            ),
            deviceData = LoginRequest.DeviceData(
                deviceId = "test-device-id-refresh",
                osType = MemberDevice.OsType.IOS,
                appVersion = "1.0.0",
                osVersion = "15.0.0",
                deviceModel = "iPhone"
            )
        )
        val loginResult = sut.login(loginData)
        val refreshToken = loginResult.second.refreshToken
        
        val refreshRequest = TokenRefreshRequest(
            refreshToken = refreshToken,
            deviceId = "test-device-id-refresh"
        )
        
        // when
        val result = sut.refreshToken(refreshRequest)
        
        // then
        assertThat(result.accessToken).isNotBlank
        assertThat(result.refreshToken).isNotBlank
    }
    
    @Test
    fun `토큰 재발급 실패 - 유효하지 않은 리프레시 토큰인 경우 예외를 던진다`() {
        // given
        val refreshRequest = TokenRefreshRequest(
            refreshToken = "invalid-refresh-token",
            deviceId = "test-device-id"
        )
        
        // when & then
        assertThatThrownBy { sut.refreshToken(refreshRequest) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JWT_REFRESH_TOKEN_INVALID)
    }
    
    @Test
    fun `앱 탈퇴 성공 - 회원을 탈퇴시킨다`() {
        // given
        val member = memberPersistFactory.createMember()
        
        // when & then (예외가 발생하지 않으면 성공)
        assertThatCode { 
            sut.withdrawByApp(member)
        }.doesNotThrowAnyException()
    }
    
    @Test
    fun `탈퇴된 회원 조회 시 예외를 던진다`() {
        // given
        val member = memberPersistFactory.createMember()
        sut.withdrawByApp(member)
        
        // when & then
        assertThatThrownBy { memberService.getActiveMemberBySeq(member.memberSeq) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WITHDRAWAL_USER)
    }
} 