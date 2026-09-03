package com.fillsa.app.service.members.quote

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import com.fillsa.util.exception.BusinessException
import com.fillsa.app.api.members.quote.AnswerRequest
import com.fillsa.app.fixture.member.persist.MemberPersistFactory
import com.fillsa.app.fixture.quote.entity.QuoteEntityFactory
import com.fillsa.app.fixture.quote.persist.QuotePersistFactory
import com.fillsa.service.member.MemberQuoteRepository
import com.fillsa.service.member.MemberStreakRepository
import java.time.LocalDate

/**
 * 오늘의 질문 답변 저장 검증.
 *
 * 핵심: 답변은 필사 완료·연속에 영향을 주지 않는다.
 * 사양: docs/home-renewal-api-plan.md §3-2, §11-1
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberQuoteAnswerServiceTest @Autowired constructor(
    private val sut: MemberQuoteUpdateService,
    private val memberPersistFactory: MemberPersistFactory,
    private val quotePersistFactory: QuotePersistFactory,
    private val memberQuoteRepository: MemberQuoteRepository,
    private val memberStreakRepository: MemberStreakRepository,
) {

    @Test
    fun `답변 저장 성공 - MemberQuote 가 없으면 생성한다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())

        // when
        val result = sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("오늘의 답변"))

        // then
        assertThat(result.answer).isEqualTo("오늘의 답변")
        assertThat(result.answeredAt).isNotNull()
        assertThat(memberQuoteRepository.findByMemberAndDailyQuote(member, daily)?.answer)
            .isEqualTo("오늘의 답변")
    }

    @Test
    fun `답변 저장 성공 - 기존 답변을 수정한다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())
        sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("첫 답변"))

        // when
        val result = sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("수정된 답변"))

        // then
        assertThat(result.answer).isEqualTo("수정된 답변")
        assertThat(memberQuoteRepository.findAllByMemberAndQuoteDateBetween(
            member, LocalDate.now(), LocalDate.now()
        )).hasSize(1)
    }

    @Test
    fun `답변 저장 - 앞뒤 공백은 제거된다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())

        // when
        val result = sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("  답변  "))

        // then
        assertThat(result.answer).isEqualTo("답변")
    }

    @Test
    fun `답변 저장 실패 - 공백만 입력하면 거부한다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())

        // when & then
        assertThatThrownBy { sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("   ")) }
            .isInstanceOf(BusinessException::class.java)
    }

    @Test
    fun `답변 저장 - 200자는 통과하고 201자는 거부한다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())

        // when & then
        assertThat(sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("가".repeat(200))).answer)
            .hasSize(200)

        assertThatThrownBy { sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("가".repeat(201))) }
            .isInstanceOf(BusinessException::class.java)
    }

    @Test
    fun `답변 저장 실패 - 미래 날짜에는 답변할 수 없다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now().plusDays(1))

        // when & then
        assertThatThrownBy { sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("미래 답변")) }
            .isInstanceOf(BusinessException::class.java)
    }

    @Test
    fun `답변 저장 실패 - 존재하지 않는 dailyQuoteSeq`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()

        // when & then
        assertThatThrownBy { sut.saveAnswer(member, -1L, AnswerRequest("답변")) }
            .isInstanceOf(BusinessException::class.java)
    }

    @Test
    fun `답변은 필사 완료로 처리되지 않는다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())

        // when
        sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("답변만 작성"))

        // then — §11-1
        val saved = memberQuoteRepository.findByMemberAndDailyQuote(member, daily)!!
        assertThat(saved.completed).isFalse()
        assertThat(saved.todayCompleted).isFalse()
    }

    @Test
    fun `답변은 연속 필사를 증가시키지 않는다`() {
        // given
        val (member, streak) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())
        val before = streak.currentStreak
        val beforeLastWritten = streak.lastWrittenDate

        // when
        sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("답변만 작성"))

        // then — §11-1
        val after = memberStreakRepository.findByMember(member)!!
        assertThat(after.currentStreak).isEqualTo(before)
        assertThat(after.lastWrittenDate).isEqualTo(beforeLastWritten)
    }

    @Test
    fun `답변만 작성한 날도 조회 대상에 포함된다`() {
        // given
        val (member, _) = memberPersistFactory.createMemberWithStreak()
        val daily = persistQuote(LocalDate.now())
        sut.saveAnswer(member, daily.dailyQuoteSeq, AnswerRequest("답변"))

        // when
        val saved = memberQuoteRepository.findByMemberAndDailyQuote(member, daily)!!

        // then — §5-2
        assertThat(saved.isViewQuoteData()).isTrue()
    }

    private fun persistQuote(quoteDate: LocalDate) =
        quotePersistFactory.createQuoteWithDailyQuote(
            dailyQuote = QuoteEntityFactory.dailyQuote(quoteDate = quoteDate)
        ).second
}
