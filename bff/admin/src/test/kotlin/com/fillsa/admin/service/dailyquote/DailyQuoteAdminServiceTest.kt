package com.fillsa.admin.service.dailyquote

import com.fillsa.admin.api.dailyquote.AssignDailyQuoteRequest
import com.fillsa.admin.fixture.AdminFixtures
import com.fillsa.admin.repository.DailyQuoteAdminRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DailyQuoteAdminServiceTest @Autowired constructor(
    private val sut: DailyQuoteAdminService,
    private val adminFixtures: AdminFixtures,
    private val dailyQuoteAdminRepository: DailyQuoteAdminRepository,
) {

    @Test
    fun `월별 현황 - 모든 날짜를 배정 여부와 함께 반환`() {
        val quote = adminFixtures.quote()
        adminFixtures.dailyQuote(quote, LocalDate.of(2026, 6, 10), "수")

        val result = sut.monthView("2026-06")

        assertThat(result.yearMonth).isEqualTo("2026-06")
        assertThat(result.days).hasSize(30) // 6월은 30일
        assertThat(result.assignedCount).isEqualTo(1)
        assertThat(result.unassignedCount).isEqualTo(29)

        val day10 = result.days.first { it.date == LocalDate.of(2026, 6, 10) }
        assertThat(day10.assigned).isTrue()
        assertThat(day10.quote).isNotNull()
    }

    @Test
    fun `배정 - quoteDayOfWeek 를 서버가 계산한다`() {
        val quote = adminFixtures.quote()
        // 2026-06-10 은 수요일
        val result = sut.assign(LocalDate.of(2026, 6, 10), AssignDailyQuoteRequest(quote.quoteSeq))

        assertThat(result.assigned).isTrue()
        assertThat(result.dayOfWeek).isEqualTo("수")
        assertThat(result.quote?.quoteSeq).isEqualTo(quote.quoteSeq)
    }

    @Test
    fun `배정 - 이미 배정된 날짜는 교체된다`() {
        val q1 = adminFixtures.quote(korQuote = "원래")
        val q2 = adminFixtures.quote(korQuote = "교체")
        val date = LocalDate.of(2026, 6, 10)

        sut.assign(date, AssignDailyQuoteRequest(q1.quoteSeq))
        sut.assign(date, AssignDailyQuoteRequest(q2.quoteSeq))

        val assignments = dailyQuoteAdminRepository.findAllByQuoteDateBetween(date, date)
        assertThat(assignments).hasSize(1)
        assertThat(assignments[0].quote.quoteSeq).isEqualTo(q2.quoteSeq)
    }

    @Test
    fun `자동 배정 - 미사용 명언을 우선 배정한다`() {
        // q1 은 이미 6월 한 날에 배정됨(사용됨), q2 는 미사용
        val q1 = adminFixtures.quote(korQuote = "사용됨")
        val q2 = adminFixtures.quote(korQuote = "미사용")
        adminFixtures.dailyQuote(q1, LocalDate.of(2026, 6, 1), "월")

        val result = sut.autoAssign("2026-06")

        // 6월 30일 중 1일은 이미 배정 → 29일 자동 배정
        assertThat(result.assignedCount).isEqualTo(29)
        assertThat(result.assignedDates).doesNotContain(LocalDate.of(2026, 6, 1))

        // 첫 미배정일(6/2)에는 미사용 명언 q2 가 배정되어야 한다
        val day2 = dailyQuoteAdminRepository.findByQuoteDate(LocalDate.of(2026, 6, 2))
        assertThat(day2?.quote?.quoteSeq).isEqualTo(q2.quoteSeq)
    }

    @Test
    fun `자동 배정 - 삭제된 명언은 배정하지 않는다`() {
        adminFixtures.quote(korQuote = "정상")
        val deleted = adminFixtures.quote(korQuote = "삭제됨", delYn = "Y")

        val result = sut.autoAssign("2026-06")

        val usedQuoteSeqs = dailyQuoteAdminRepository.findAllByQuoteDateBetween(
            LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
        ).map { it.quote.quoteSeq }.toSet()

        assertThat(result.assignedCount).isEqualTo(30)
        assertThat(usedQuoteSeqs).doesNotContain(deleted.quoteSeq)
    }

    @Test
    fun `자동 배정 - 이미 모두 배정된 달은 idempotent(0건)`() {
        adminFixtures.quote()
        sut.autoAssign("2026-06") // 30일 전부 배정

        val second = sut.autoAssign("2026-06")

        assertThat(second.assignedCount).isEqualTo(0)
        assertThat(second.assignedDates).isEmpty()
    }
}
