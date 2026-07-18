package com.fillsa.admin.service.auth

import com.fillsa.admin.common.security.AdminJwtTokenProvider
import com.fillsa.admin.fixture.AdminFixtures
import com.fillsa.service.admin.AdminRepository
import com.fillsa.service.admin.AdminRole
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminAuthServiceTest @Autowired constructor(
    private val sut: AdminAuthService,
    private val adminFixtures: AdminFixtures,
    private val adminJwtTokenProvider: AdminJwtTokenProvider,
    private val adminRepository: AdminRepository,
) {

    @Test
    fun `로그인 성공 - 토큰과 프로필을 반환하고 lastLoginAt 을 기록한다`() {
        // given
        val admin = adminFixtures.admin(loginId = "super1", rawPassword = "pw12345678", role = AdminRole.SUPER)

        // when
        val result = sut.login(LoginRequest(loginId = "super1", password = "pw12345678"))

        // then
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
        assertThat(result.expiresIn).isGreaterThan(0)
        assertThat(result.admin.adminSeq).isEqualTo(admin.adminSeq)
        assertThat(result.admin.role).isEqualTo(AdminRole.SUPER)

        // 발급된 액세스 토큰은 admin 마커와 role 클레임을 담는다
        val claims = adminJwtTokenProvider.parseClaims(result.accessToken)
        assertThat(adminJwtTokenProvider.isAdminToken(claims)).isTrue()
        assertThat(adminJwtTokenProvider.getRole(claims)).isEqualTo(AdminRole.SUPER)

        // lastLoginAt 기록
        assertThat(adminRepository.findById(admin.adminSeq).get().lastLoginAt).isNotNull()
    }

    @Test
    fun `로그인 실패 - 비밀번호 불일치 시 ADMIN_LOGIN_FAILED`() {
        adminFixtures.admin(loginId = "user1", rawPassword = "correct-pw")

        assertThatThrownBy { sut.login(LoginRequest(loginId = "user1", password = "wrong-pw")) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_LOGIN_FAILED)
    }

    @Test
    fun `로그인 실패 - 존재하지 않는 loginId 시 ADMIN_LOGIN_FAILED`() {
        assertThatThrownBy { sut.login(LoginRequest(loginId = "no-such", password = "whatever")) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_LOGIN_FAILED)
    }

    @Test
    fun `로그인 실패 - 비활성(activeYn=N) 계정은 ADMIN_ACCOUNT_INACTIVE`() {
        adminFixtures.admin(loginId = "inactive1", rawPassword = "pw12345678", activeYn = "N")

        assertThatThrownBy { sut.login(LoginRequest(loginId = "inactive1", password = "pw12345678")) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_ACCOUNT_INACTIVE)
    }

    @Test
    fun `토큰 재발급 성공 - 유효한 리프레시 토큰으로 새 토큰을 발급한다`() {
        val admin = adminFixtures.admin(loginId = "refresh1", rawPassword = "pw12345678")
        val login = sut.login(LoginRequest(loginId = "refresh1", password = "pw12345678"))

        val result = sut.refresh(RefreshRequest(refreshToken = login.refreshToken))

        assertThat(result.accessToken).isNotBlank()
        assertThat(result.admin.adminSeq).isEqualTo(admin.adminSeq)
    }

    @Test
    fun `토큰 재발급 실패 - 액세스 토큰을 리프레시로 사용하면 거부한다`() {
        adminFixtures.admin(loginId = "refresh2", rawPassword = "pw12345678")
        val login = sut.login(LoginRequest(loginId = "refresh2", password = "pw12345678"))

        assertThatThrownBy { sut.refresh(RefreshRequest(refreshToken = login.accessToken)) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADMIN_TOKEN_INVALID)
    }
}
