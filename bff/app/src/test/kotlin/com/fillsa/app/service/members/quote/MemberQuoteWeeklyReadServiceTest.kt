package com.fillsa.app.service.members.quote

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.api.members.quote.DayQuoteData
import com.fillsa.app.fixture.member.persist.MemberPersistFactory
import com.fillsa.app.fixture.quote.entity.QuoteEntityFactory
import com.fillsa.app.fixture.quote.persist.QuotePersistFactory
import java.time.LocalDate

/**
 * 홈 상단 롤링 7일 조회(weekly) + 단일 날짜 조회(dailyQuoteV2) 검증.
 * 사양: docs/home-renewal-api-plan.md §3-1, §3-3
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberQuoteWeeklyReadServiceTest @Autowired constructor(
    private val sut: MemberQuoteReadService,
    private val memberPersistFactory: MemberPersistFactory,
    private val quotePersistFactory: QuotePersistFactory,
    private val redisTemplate: StringRedisTemplate,
) {

    /**
     * 명언 캐시는 Redis 라 @Transactional 롤백으로 정리되지 않는다.
     * 앞선 테스트가 남긴 캐시가 다음 테스트로 새지 않도록 매번 비운다.
     */
    @BeforeEach
    fun clearQuoteCache() {
        redisTemplate.delete("daily_quotes_sorted")
    }

    @Test
    fun `주간 조회 - endDate 생략 시 오늘이 마지막 칸인 7일 창을 반환한다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val today = LocalDate.now()

        // when
        val result = sut.weeklyQuotes(member, null)

        // then
        assertThat(result.endDate).isEqualTo(today)
        assertThat(result.startDate).isEqualTo(today.minusDays(6))
        assertThat(result.days).hasSize(7)
        assertThat(result.days.map { it.date })
            .containsExactlyElementsOf((0..6).map { today.minusDays(6L - it) })
    }

    @Test
    fun `주간 조회 - endDate 가 미래면 오늘로 보정한다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val today = LocalDate.now()

        // when
        val result = sut.weeklyQuotes(member, today.plusDays(30))

        // then — 미래 문장이 노출되지 않아야 한다
        assertThat(result.endDate).isEqualTo(today)
        assertThat(result.days.last().date).isEqualTo(today)
        assertThat(result.days).noneMatch { it.date.isAfter(today) }
    }

    @Test
    fun `주간 조회 - 이전 창은 endDate 기준 7일 전이며 날짜가 겹치지 않는다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val today = LocalDate.now()

        // when
        val current = sut.weeklyQuotes(member, null)
        val previous = sut.weeklyQuotes(member, current.endDate.minusDays(7))

        // then
        assertThat(previous.endDate).isEqualTo(today.minusDays(7))
        assertThat(previous.startDate).isEqualTo(today.minusDays(13))

        val currentDates = current.days.map { it.date }.toSet()
        val previousDates = previous.days.map { it.date }.toSet()
        assertThat(currentDates.intersect(previousDates)).isEmpty()
        assertThat(previous.endDate.plusDays(1)).isEqualTo(current.startDate)
    }

    @Test
    fun `주간 조회 - state 는 오늘이면 today, 완료면 done, 나머지는 none 이다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val today = LocalDate.now()
        val past = today.minusDays(3)

        // 오늘 = 미완료, 과거 = 완료
        persistQuote(quoteDate = today)
        val pastDaily = persistQuote(quoteDate = past)
        quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(member = member, dailyQuote = pastDaily, completed = true)
        )

        // when
        val days = sut.weeklyQuotes(member, null).days.associateBy { it.date }

        // then
        assertThat(days[today]?.state).isEqualTo(DayQuoteData.STATE_TODAY)
        assertThat(days[past]?.state).isEqualTo(DayQuoteData.STATE_DONE)
        assertThat(days[today.minusDays(1)]?.state).isEqualTo(DayQuoteData.STATE_NONE)
    }

    @Test
    fun `주간 조회 - 오늘이면서 완료면 done 을 우선한다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val today = LocalDate.now()
        val daily = persistQuote(quoteDate = today)
        quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member, dailyQuote = daily, completed = true, todayCompleted = true
            )
        )

        // when
        val days = sut.weeklyQuotes(member, null).days.associateBy { it.date }

        // then
        assertThat(days[today]?.state).isEqualTo(DayQuoteData.STATE_DONE)
    }

    @Test
    fun `주간 조회 - 명언이 배정되지 않은 날짜도 칸을 채워 7일을 유지한다`() {
        // given — 명언을 하나도 만들지 않는다
        val (member, _) = memberPersistFactory.createMemberWithStreak()

        // when
        val result = sut.weeklyQuotes(member, null)

        // then
        assertThat(result.days).hasSize(7)
        assertThat(result.days).allMatch { it.dailyQuoteSeq == null }
        assertThat(result.days).allMatch { it.korQuote == null }
    }

    @Test
    fun `주간 조회 - 답변만 작성한 날도 응답에 포함된다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val target = LocalDate.now().minusDays(2)
        val daily = persistQuote(quoteDate = target)
        quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member,
                dailyQuote = daily,
                answer = "답변만 작성한 날"
            )
        )

        // when
        val days = sut.weeklyQuotes(member, null).associateDays()

        // then — 필사도 좋아요도 없지만 답변이 실려야 한다 (§5-2)
        assertThat(days[target]?.answer).isEqualTo("답변만 작성한 날")
        assertThat(days[target]?.completed).isFalse()
        assertThat(days[target]?.state).isEqualTo(DayQuoteData.STATE_NONE)
    }

    @Test
    fun `주간 조회 - 회고 질문이 응답에 포함된다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val target = LocalDate.now().minusDays(1)
        val quote = QuoteEntityFactory.quote().apply {
            questionKo = "한글 질문"
            questionEn = "English question"
        }
        persistQuote(quoteDate = target, quote = quote)

        // when
        val days = sut.weeklyQuotes(member, null).associateDays()

        // then
        assertThat(days[target]?.questionKo).isEqualTo("한글 질문")
        assertThat(days[target]?.questionEn).isEqualTo("English question")
    }

    @Test
    fun `단일 날짜 조회 - 미래 날짜는 문장과 질문을 반환하지 않는다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val future = LocalDate.now().plusDays(1)
        persistQuote(quoteDate = future)

        // when
        val result = sut.dailyQuoteV2(member, future)

        // then
        assertThat(result.date).isEqualTo(future)
        assertThat(result.dailyQuoteSeq).isNull()
        assertThat(result.korQuote).isNull()
        assertThat(result.questionKo).isNull()
        assertThat(result.state).isEqualTo(DayQuoteData.STATE_NONE)
    }

    @Test
    fun `단일 날짜 조회 - 응답이 주간 조회 days 원소와 동일한 값을 가진다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val target = LocalDate.now().minusDays(1)
        val daily = persistQuote(quoteDate = target)
        quotePersistFactory.createMemberQuote(
            QuoteEntityFactory.memberQuote(
                member = member, dailyQuote = daily, likeYn = "Y", completed = true
            )
        )

        // when
        val fromDaily = sut.dailyQuoteV2(member, target)
        val fromWeekly = sut.weeklyQuotes(member, null).associateDays()[target]

        // then
        assertThat(fromDaily).isEqualTo(fromWeekly)
    }

    private fun persistQuote(
        quoteDate: LocalDate,
        quote: com.fillsa.service.quote.Quote = QuoteEntityFactory.quote()
    ) = quotePersistFactory.createQuoteWithDailyQuote(
        quote = quote,
        dailyQuote = QuoteEntityFactory.dailyQuote(quoteDate = quoteDate)
    ).second

    private fun com.fillsa.app.api.members.quote.WeeklyQuoteResponse.associateDays() =
        days.associateBy { it.date }
}
