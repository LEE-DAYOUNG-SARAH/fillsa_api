package com.fillsa.app.service.members.quote

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.fillsa.app.fixture.quote.entity.QuoteEntityFactory
import java.time.LocalDate

/**
 * 연속 필사 회귀 방지.
 *
 * 이번 리뉴얼에서 연속 로직은 변경하지 않기로 했다(docs/home-renewal-api-plan.md §11-2).
 * 현재 동작을 고정해 두어 의도치 않은 변경을 즉시 잡는다.
 *
 * 용어 주의 — '연속필사'는 두 의미로 쓰인다.
 *  ① 행위: 당일 완전 필사 → MemberQuote.todayCompleted
 *  ② 길이: 며칠 연속인가 → MemberStreak.currentStreak
 */
class MemberStreakRegressionTest {

    private val today = LocalDate.of(2026, 9, 3)

    @Test
    fun `어제 필사했고 오늘 필사하면 1 증가한다`() {
        val streak = QuoteEntityFactory.memberStreak(
            currentStreak = 5, maxStreak = 5, lastWrittenDate = today.minusDays(1)
        )

        streak.recordTodayCompletion(today)

        assertThat(streak.currentStreak).isEqualTo(6)
        assertThat(streak.maxStreak).isEqualTo(6)
        assertThat(streak.lastWrittenDate).isEqualTo(today)
    }

    @Test
    fun `하루라도 건너뛰면 1로 리셋된다`() {
        val streak = QuoteEntityFactory.memberStreak(
            currentStreak = 10, maxStreak = 10, lastWrittenDate = today.minusDays(2)
        )

        streak.recordTodayCompletion(today)

        assertThat(streak.currentStreak).isEqualTo(1)
        assertThat(streak.maxStreak).isEqualTo(10) // 최고 기록은 유지
    }

    @Test
    fun `첫 필사면 1이 된다`() {
        val streak = QuoteEntityFactory.memberStreak(lastWrittenDate = null)

        streak.recordTodayCompletion(today)

        assertThat(streak.currentStreak).isEqualTo(1)
        assertThat(streak.maxStreak).isEqualTo(1)
    }

    @Test
    fun `같은 날 중복 호출은 무시된다`() {
        val streak = QuoteEntityFactory.memberStreak(
            currentStreak = 3, maxStreak = 3, lastWrittenDate = today.minusDays(1)
        )

        streak.recordTodayCompletion(today)
        streak.recordTodayCompletion(today)
        streak.recordTodayCompletion(today)

        assertThat(streak.currentStreak).isEqualTo(4)
    }

    @Test
    fun `조회 시 - 오늘 필사했으면 값을 그대로 반환한다`() {
        val streak = QuoteEntityFactory.memberStreak(currentStreak = 7, lastWrittenDate = today)

        assertThat(streak.currentStreakAsOf(today)).isEqualTo(7)
        assertThat(streak.isTodayWritten(today)).isTrue()
    }

    @Test
    fun `조회 시 - 어제까지 했고 오늘 아직이면 값을 유지한다`() {
        val streak = QuoteEntityFactory.memberStreak(
            currentStreak = 7, lastWrittenDate = today.minusDays(1)
        )

        assertThat(streak.currentStreakAsOf(today)).isEqualTo(7)
        assertThat(streak.isTodayWritten(today)).isFalse()
    }

    @Test
    fun `조회 시 - 이틀 이상 지났으면 0으로 보정한다`() {
        val streak = QuoteEntityFactory.memberStreak(
            currentStreak = 100, lastWrittenDate = today.minusDays(2)
        )

        // DB 값은 그대로 남지만 표시값은 0
        assertThat(streak.currentStreak).isEqualTo(100)
        assertThat(streak.currentStreakAsOf(today)).isZero()
    }

    @Test
    fun `조회 시 - 필사 이력이 없으면 0이다`() {
        val streak = QuoteEntityFactory.memberStreak(lastWrittenDate = null)

        assertThat(streak.currentStreakAsOf(today)).isZero()
        assertThat(streak.isTodayWritten(today)).isFalse()
    }

    @Test
    fun `필사 완료 - 오늘이면 completed 와 todayCompleted 가 모두 세팅된다`() {
        val memberQuote = QuoteEntityFactory.memberQuote()

        memberQuote.complete(LocalDate.now())

        assertThat(memberQuote.completed).isTrue()
        assertThat(memberQuote.todayCompleted).isTrue()
    }

    @Test
    fun `필사 완료 - 과거 날짜면 completed 만 세팅되고 todayCompleted 는 false 다`() {
        val memberQuote = QuoteEntityFactory.memberQuote()

        memberQuote.complete(LocalDate.now().minusDays(3))

        assertThat(memberQuote.completed).isTrue()
        assertThat(memberQuote.todayCompleted).isFalse()
    }

    @Test
    fun `이미지 업로드는 필사 완료로 인정된다`() {
        // 기존 기획 사양 — raw/shared/작업/연속필사 기획.md
        val memberQuote = QuoteEntityFactory.memberQuote()

        assertThat(memberQuote.shouldMarkImageCompleted("https://cdn/a.jpg")).isTrue()
    }

    @Test
    fun `타이핑은 원문과 정확히 일치해야 완료로 인정된다`() {
        val memberQuote = QuoteEntityFactory.memberQuote()

        assertThat(
            memberQuote.shouldMarkTypingCompleted("원문", null, "원문", "origin")
        ).isTrue()

        assertThat(
            memberQuote.shouldMarkTypingCompleted("원문 일부", null, "원문", "origin")
        ).isFalse()
    }

    @Test
    fun `조회 대상 판정 - 완료 좋아요 답변 중 하나라도 있으면 포함된다`() {
        assertThat(QuoteEntityFactory.memberQuote(completed = true).isViewQuoteData()).isTrue()
        assertThat(QuoteEntityFactory.memberQuote(likeYn = "Y").isViewQuoteData()).isTrue()
        assertThat(QuoteEntityFactory.memberQuote(answer = "답변").isViewQuoteData()).isTrue()

        assertThat(QuoteEntityFactory.memberQuote().isViewQuoteData()).isFalse()
        assertThat(QuoteEntityFactory.memberQuote(answer = "  ").isViewQuoteData()).isFalse()
    }
}
