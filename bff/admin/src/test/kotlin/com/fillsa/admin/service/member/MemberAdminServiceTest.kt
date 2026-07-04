package com.fillsa.admin.service.member

import com.fillsa.admin.fixture.AdminFixtures
import com.fillsa.service.member.Member
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberAdminServiceTest @Autowired constructor(
    private val sut: MemberAdminService,
    private val adminFixtures: AdminFixtures,
) {

    @Test
    fun `목록 - 기본 정렬 가입일(createdAt) desc`() {
        val m1 = adminFixtures.member(nickname = "첫번째회원")
        val m2 = adminFixtures.member(nickname = "두번째회원")

        val page = sut.list(PageRequest.of(0, 10), null, null, null)

        assertThat(page.content).hasSize(2)
        // 기본 정렬 createdAt desc → 나중에 저장한 m2 가 먼저
        assertThat(page.content[0].memberSeq).isEqualTo(m2.memberSeq)
        assertThat(page.content[1].memberSeq).isEqualTo(m1.memberSeq)
        assertThat(page.page.totalElements).isEqualTo(2)
    }

    @Test
    fun `목록 - keyword 는 닉네임·OAuth ID 부분일치 검색`() {
        adminFixtures.member(nickname = "행복한회원", oauthId = "kakao-111")
        adminFixtures.member(nickname = "슬픈회원", oauthId = "kakao-222")

        val byNickname = sut.list(PageRequest.of(0, 10), "행복", null, null)
        assertThat(byNickname.content).hasSize(1)
        assertThat(byNickname.content[0].nickname).isEqualTo("행복한회원")

        val byOauthId = sut.list(PageRequest.of(0, 10), "222", null, null)
        assertThat(byOauthId.content).hasSize(1)
        assertThat(byOauthId.content[0].nickname).isEqualTo("슬픈회원")
    }

    @Test
    fun `목록 - oauthProvider 필터`() {
        adminFixtures.member(nickname = "카카오회원", oauthProvider = Member.OAuthProvider.KAKAO)
        adminFixtures.member(nickname = "구글회원", oauthProvider = Member.OAuthProvider.GOOGLE)

        val page = sut.list(PageRequest.of(0, 10), null, Member.OAuthProvider.GOOGLE, null)
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].nickname).isEqualTo("구글회원")
    }

    @Test
    fun `목록 - withdrawalYn 필터`() {
        adminFixtures.member(nickname = "활성회원", withdrawalYn = "N")
        adminFixtures.member(nickname = "탈퇴회원", withdrawalYn = "Y")

        val page = sut.list(PageRequest.of(0, 10), null, null, "Y")
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].nickname).isEqualTo("탈퇴회원")
    }

    @Test
    fun `목록 - 페이징`() {
        adminFixtures.member(nickname = "회원1")
        adminFixtures.member(nickname = "회원2")
        adminFixtures.member(nickname = "회원3")

        val page = sut.list(PageRequest.of(0, 2), null, null, null)

        assertThat(page.content).hasSize(2)
        assertThat(page.page.totalElements).isEqualTo(3)
        assertThat(page.page.totalPages).isEqualTo(2)
    }

    @Test
    fun `요약 - 전체·활성·탈퇴·관리자 카운트`() {
        adminFixtures.member(withdrawalYn = "N", adminYn = "N")
        adminFixtures.member(withdrawalYn = "Y", adminYn = "N")
        adminFixtures.member(withdrawalYn = "N", adminYn = "Y")

        val summary = sut.summary()

        assertThat(summary.totalCount).isEqualTo(3)
        assertThat(summary.withdrawnCount).isEqualTo(1)
        assertThat(summary.activeCount).isEqualTo(2)
        assertThat(summary.adminCount).isEqualTo(1)
    }

    @Test
    fun `상세 - 디바이스 목록·누적 필사 건수·현재 연속을 포함한다`() {
        val member = adminFixtures.member(nickname = "상세회원")
        adminFixtures.memberDevice(member, deviceModel = "iPhone 15")
        adminFixtures.memberDevice(member, deviceModel = "Galaxy S23")

        val quote = adminFixtures.quote()
        val dailyQuote1 = adminFixtures.dailyQuote(quote, LocalDate.now().minusDays(1))
        val dailyQuote2 = adminFixtures.dailyQuote(quote, LocalDate.now())
        adminFixtures.memberQuote(member, dailyQuote1, completed = true)
        adminFixtures.memberQuote(member, dailyQuote2, completed = true)

        adminFixtures.memberStreak(member, currentStreak = 5, maxStreak = 10, lastWrittenDate = LocalDate.now())

        val detail = sut.detail(member.memberSeq)

        assertThat(detail.devices).hasSize(2)
        assertThat(detail.totalCompletedCount).isEqualTo(2)
        assertThat(detail.currentStreak).isEqualTo(5)
        assertThat(detail.maxStreak).isEqualTo(10)
    }

    @Test
    fun `상세 - 필사·연속 이력이 없으면 0으로 응답한다`() {
        val member = adminFixtures.member(nickname = "신규회원")

        val detail = sut.detail(member.memberSeq)

        assertThat(detail.devices).isEmpty()
        assertThat(detail.totalCompletedCount).isEqualTo(0)
        assertThat(detail.currentStreak).isEqualTo(0)
        assertThat(detail.maxStreak).isEqualTo(0)
    }

    @Test
    fun `상세 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.detail(999_999L) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }

    @Test
    fun `관리자 권한 토글 - N에서 Y로 변경`() {
        val member = adminFixtures.member(adminYn = "N")

        val result = sut.toggleAdminRole(member.memberSeq, "Y")

        assertThat(result.adminYn).isEqualTo("Y")
    }

    @Test
    fun `관리자 권한 토글 - Y에서 N으로 변경`() {
        val member = adminFixtures.member(adminYn = "Y")

        val result = sut.toggleAdminRole(member.memberSeq, "N")

        assertThat(result.adminYn).isEqualTo("N")
    }

    @Test
    fun `관리자 권한 토글 - adminYn 이 Y 또는 N 이 아니면 400(INVALID_VALUE)`() {
        val member = adminFixtures.member()

        assertThatThrownBy { sut.toggleAdminRole(member.memberSeq, "X") }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VALUE)
    }

    @Test
    fun `관리자 권한 토글 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.toggleAdminRole(999_999L, "Y") }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
