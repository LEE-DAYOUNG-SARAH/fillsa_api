package com.fillsa.admin.service.memberquote

import com.fillsa.admin.fixture.AdminFixtures
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
class MemberQuoteAdminServiceTest @Autowired constructor(
    private val sut: MemberQuoteAdminService,
    private val adminFixtures: AdminFixtures,
) {

    @Test
    fun `목록 - 기본 정렬 명언 일자(quoteDate) desc`() {
        val member = adminFixtures.member()
        val quote = adminFixtures.quote()
        val dailyQuote1 = adminFixtures.dailyQuote(quote, LocalDate.now().minusDays(1))
        val dailyQuote2 = adminFixtures.dailyQuote(quote, LocalDate.now())
        val mq1 = adminFixtures.memberQuote(member, dailyQuote1)
        val mq2 = adminFixtures.memberQuote(member, dailyQuote2)

        val page = sut.list(PageRequest.of(0, 10), null, null, null)

        assertThat(page.content).hasSize(2)
        // 기본 정렬 quoteDate desc → 더 최근 명언 일자(mq2) 가 먼저
        assertThat(page.content[0].memberQuoteSeq).isEqualTo(mq2.memberQuoteSeq)
        assertThat(page.content[1].memberQuoteSeq).isEqualTo(mq1.memberQuoteSeq)
        assertThat(page.page.totalElements).isEqualTo(2)
    }

    @Test
    fun `목록 - keyword 는 회원 닉네임·명언(kor,eng) 부분일치 검색`() {
        val member1 = adminFixtures.member(nickname = "행복회원")
        val member2 = adminFixtures.member(nickname = "슬픔회원")
        val quote1 = adminFixtures.quote(korQuote = "행복한 명언", engQuote = "Happy quote")
        val quote2 = adminFixtures.quote(korQuote = "슬픈 명언", engQuote = "Sad quote")
        val dailyQuote1 = adminFixtures.dailyQuote(quote1, LocalDate.now().minusDays(1))
        val dailyQuote2 = adminFixtures.dailyQuote(quote2, LocalDate.now())
        adminFixtures.memberQuote(member1, dailyQuote1)
        adminFixtures.memberQuote(member2, dailyQuote2)

        val byNickname = sut.list(PageRequest.of(0, 10), "행복회원", null, null)
        assertThat(byNickname.content).hasSize(1)
        assertThat(byNickname.content[0].nickname).isEqualTo("행복회원")

        val byEngQuote = sut.list(PageRequest.of(0, 10), "Sad", null, null)
        assertThat(byEngQuote.content).hasSize(1)
        assertThat(byEngQuote.content[0].korQuote).isEqualTo("슬픈 명언")
    }

    @Test
    fun `목록 - quoteDate 필터`() {
        val member = adminFixtures.member()
        val quote = adminFixtures.quote()
        val dailyQuote1 = adminFixtures.dailyQuote(quote, LocalDate.now().minusDays(1))
        val dailyQuote2 = adminFixtures.dailyQuote(quote, LocalDate.now())
        adminFixtures.memberQuote(member, dailyQuote1)
        adminFixtures.memberQuote(member, dailyQuote2)

        val page = sut.list(PageRequest.of(0, 10), null, LocalDate.now(), null)
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].quoteDate).isEqualTo(LocalDate.now())
    }

    @Test
    fun `목록 - completed 필터`() {
        val member = adminFixtures.member()
        val quote = adminFixtures.quote()
        val dailyQuote1 = adminFixtures.dailyQuote(quote, LocalDate.now().minusDays(1))
        val dailyQuote2 = adminFixtures.dailyQuote(quote, LocalDate.now())
        adminFixtures.memberQuote(member, dailyQuote1, completed = true)
        adminFixtures.memberQuote(member, dailyQuote2, completed = false)

        val page = sut.list(PageRequest.of(0, 10), null, null, true)
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].completed).isTrue()
    }

    @Test
    fun `목록 - 페이징`() {
        val member = adminFixtures.member()
        val quote = adminFixtures.quote()
        repeat(3) { i ->
            val dailyQuote = adminFixtures.dailyQuote(quote, LocalDate.now().minusDays(i.toLong()))
            adminFixtures.memberQuote(member, dailyQuote)
        }

        val page = sut.list(PageRequest.of(0, 2), null, null, null)

        assertThat(page.content).hasSize(2)
        assertThat(page.page.totalElements).isEqualTo(3)
        assertThat(page.page.totalPages).isEqualTo(2)
    }

    @Test
    fun `상세 - 회원·명언 정보와 배정 건수를 포함한다`() {
        val member = adminFixtures.member(nickname = "상세회원")
        val quote = adminFixtures.quote(korQuote = "상세 명언")
        val dailyQuote = adminFixtures.dailyQuote(quote, LocalDate.now())
        adminFixtures.dailyQuote(quote, LocalDate.now().minusDays(1)) // 같은 명언의 추가 배정 이력
        val memberQuote = adminFixtures.memberQuote(member, dailyQuote, memo = "메모", likeYn = "Y")

        val detail = sut.detail(memberQuote.memberQuoteSeq)

        assertThat(detail.memberSeq).isEqualTo(member.memberSeq)
        assertThat(detail.nickname).isEqualTo("상세회원")
        assertThat(detail.korQuote).isEqualTo("상세 명언")
        assertThat(detail.memo).isEqualTo("메모")
        assertThat(detail.likeYn).isEqualTo("Y")
        assertThat(detail.quote.assignedCount).isEqualTo(2)
    }

    @Test
    fun `상세 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.detail(999_999L) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
