package com.fillsa.admin.service.quote

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
class QuoteAdminServiceTest @Autowired constructor(
    private val sut: QuoteAdminService,
    private val adminFixtures: AdminFixtures,
    private val quoteAdminRepository: QuoteAdminQueryRepository,
) {

    @Test
    fun `목록 - 기본 정렬 quoteSeq desc, 삭제 제외`() {
        val q1 = adminFixtures.quote(korQuote = "첫번째")
        val q2 = adminFixtures.quote(korQuote = "두번째")
        adminFixtures.quote(korQuote = "삭제됨", delYn = "Y")

        val page = sut.list(PageRequest.of(0, 10), null, null)

        assertThat(page.content).hasSize(2)
        // 기본 정렬 quoteSeq desc → 나중에 저장한 q2 가 먼저
        assertThat(page.content[0].quoteSeq).isEqualTo(q2.quoteSeq)
        assertThat(page.content[1].quoteSeq).isEqualTo(q1.quoteSeq)
        assertThat(page.page.totalElements).isEqualTo(2)
    }

    @Test
    fun `목록 - keyword 는 명언·작가 부분일치 검색`() {
        adminFixtures.quote(korQuote = "행복한 명언", korAuthor = "김작가")
        adminFixtures.quote(korQuote = "슬픈 문장", korAuthor = "이작가")

        val byQuote = sut.list(PageRequest.of(0, 10), "행복", null)
        assertThat(byQuote.content).hasSize(1)
        assertThat(byQuote.content[0].korQuote).isEqualTo("행복한 명언")

        val byAuthor = sut.list(PageRequest.of(0, 10), "이작가", null)
        assertThat(byAuthor.content).hasSize(1)
        assertThat(byAuthor.content[0].korAuthor).isEqualTo("이작가")
    }

    @Test
    fun `목록 - category 필터`() {
        adminFixtures.quote(korQuote = "A", category = "동기부여")
        adminFixtures.quote(korQuote = "B", category = "사랑")

        val page = sut.list(PageRequest.of(0, 10), null, "사랑")
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].category).isEqualTo("사랑")
    }

    @Test
    fun `목록 - assignedCount 는 daily_quotes 참조 수`() {
        val quote = adminFixtures.quote(korQuote = "배정된 명언")
        adminFixtures.dailyQuote(quote, LocalDate.of(2026, 6, 1), "월")
        adminFixtures.dailyQuote(quote, LocalDate.of(2026, 6, 2), "화")

        val page = sut.list(PageRequest.of(0, 10), "배정된", null)
        assertThat(page.content[0].assignedCount).isEqualTo(2)
    }

    @Test
    fun `삭제 - 배정 이력 있으면 409(CONFLICT)`() {
        val quote = adminFixtures.quote()
        adminFixtures.dailyQuote(quote, LocalDate.of(2026, 6, 1), "월")

        assertThatThrownBy { sut.delete(quote.quoteSeq) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFLICT)
    }

    @Test
    fun `삭제 - 배정 이력 없으면 soft delete(delYn=Y)`() {
        val quote = adminFixtures.quote()

        sut.delete(quote.quoteSeq)

        val reloaded = quoteAdminRepository.findById(quote.quoteSeq).get()
        assertThat(reloaded.delYn).isEqualTo("Y")
    }

    @Test
    fun `삭제 - 존재하지 않으면 404(NOT_FOUND)`() {
        assertThatThrownBy { sut.delete(999_999L) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
    }
}
