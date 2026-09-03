package com.fillsa.app.service.members.quote

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.common.dto.PageResponse
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import com.fillsa.util.exception.ErrorCode.NOT_FOUND
import com.fillsa.service.member.Member
import com.fillsa.app.api.members.quote.*
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.member.MemberQuoteRepository
import com.fillsa.app.service.quote.DailyQuoteService
import java.time.LocalDate
import java.time.YearMonth

@Service
class MemberQuoteReadService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteService: DailyQuoteService,
    @Value("\${fillsa.ko-author-url}")
    private val koAuthorUrl: String,
    @Value("\${fillsa.en-author-url}")
    private val enAuthorUrl: String,
) {

    @Transactional(readOnly = true)
    fun dailyQuote(member: Member, quoteDate: LocalDate): MemberDailyQuoteResponse {
        val dailyQuote = dailyQuoteService.getDailyQuoteByQuoteDate(quoteDate)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 quoteDate: $quoteDate")

        val memberQuote = memberQuoteRepository.findByMemberAndDailyQuote(member, dailyQuote)

        return MemberDailyQuoteResponse.from(koAuthorUrl, enAuthorUrl, dailyQuote, memberQuote)
    }

    /**
     * 홈 상단 롤링 7일 조회. endDate 를 마지막 칸으로 하는 7일 창(endDate-6 ~ endDate)을 반환한다.
     *
     * - endDate 생략 시 서버 기준 오늘
     * - 오늘보다 미래면 오늘로 clamp — 미래 문장이 노출되지 않게 한다
     */
    @Transactional(readOnly = true)
    fun weeklyQuotes(member: Member, endDate: LocalDate?): WeeklyQuoteResponse {
        val today = LocalDate.now()
        val windowEnd = (endDate ?: today).coerceAtMost(today)
        val windowStart = windowEnd.minusDays(WeeklyQuoteResponse.WINDOW_SIZE - 1)

        val dailyQuotes = dailyQuoteService.getDailyQuotesInRange(windowStart, windowEnd)
            .associateBy { it.quoteDate }
        val memberQuotes = memberQuoteRepository
            .findAllByMemberAndQuoteDateBetween(member, windowStart, windowEnd)
            .associateBy { it.dailyQuote.quoteDate }

        val days = (0 until WeeklyQuoteResponse.WINDOW_SIZE)
            .map { windowStart.plusDays(it) }
            .map { date ->
                dailyQuotes[date]
                    ?.let { DayQuoteData.from(koAuthorUrl, enAuthorUrl, it, memberQuotes[date], today) }
                    ?: DayQuoteData.empty(date, today)
            }

        return WeeklyQuoteResponse.of(windowStart, windowEnd, days)
    }

    /**
     * 단일 날짜 조회(V2). 응답은 주간 조회의 days[] 원소와 동일한 형태다.
     * 저장 후 단일 날짜 갱신·딥링크 진입 등 보조 경로에서 사용한다.
     */
    @Transactional(readOnly = true)
    fun dailyQuoteV2(member: Member, quoteDate: LocalDate): DayQuoteData {
        val today = LocalDate.now()
        if (quoteDate.isAfter(today)) {
            return DayQuoteData.future(quoteDate)
        }

        val dailyQuote = dailyQuoteService.getDailyQuoteByQuoteDate(quoteDate)
            ?: return DayQuoteData.empty(quoteDate, today)
        val memberQuote = memberQuoteRepository.findByMemberAndDailyQuote(member, dailyQuote)

        return DayQuoteData.from(koAuthorUrl, enAuthorUrl, dailyQuote, memberQuote, today)
    }

    @Transactional(readOnly = true)
    fun monthlyQuotes(member: Member, yearMonth: YearMonth): MemberMonthlyQuoteResponse {
        if(yearMonth.isAfter(YearMonth.now())) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "현재 월 이후는 조회할 수 없습니다.")
        }

        val startDate = yearMonth.atDay(1)
        val endDate = if (yearMonth == YearMonth.now())
            LocalDate.now() else yearMonth.atEndOfMonth()

        val quotes = dailyQuoteService.getDailyQuoteByQuotMonth(startDate, endDate)
        val memberQuotes = getMemberQuotesWithContentByMonth(member, startDate, endDate)

        return MemberMonthlyQuoteResponse.from(quotes, memberQuotes)
    }

    @Transactional(readOnly = true)
    fun monthlyQuotesV2(member: Member, yearMonth: YearMonth): MemberMonthlyQuoteResponseV2 {
        if(yearMonth.isAfter(YearMonth.now())) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "현재 월 이후는 조회할 수 없습니다.")
        }

        val startDate = yearMonth.atDay(1)
        val endDate = if (yearMonth == YearMonth.now())
            LocalDate.now() else yearMonth.atEndOfMonth()

        val quotes = dailyQuoteService.getDailyQuoteByQuotMonth(startDate, endDate)
        val memberQuotes = getMemberQuotesWithContentByMonth(member, startDate, endDate)

        return MemberMonthlyQuoteResponseV2.from(koAuthorUrl, enAuthorUrl, quotes, memberQuotes)
    }

    private fun getMemberQuotesWithContentByMonth(
        member: Member,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MemberQuote> {
        val memberQuotes = memberQuoteRepository.findAllByMemberAndQuoteDateBetween(
            member = member,
            beginQuoteDate = startDate,
            endQuoteDate = endDate
        )

        return memberQuotes.filter { it.isViewQuoteData() }
    }

    @Transactional(readOnly = true)
    fun memberQuotes(
        member: Member,
        pageable: Pageable,
        request: MemberQuotesCommonRequest
    ): PageResponse<MemberQuotesResponse> {
        val memberQuotes = getMemberQuotesWithContentByRequest(member, request)

        return PageResponse.fromList(memberQuotes, pageable) { memberQuote ->
            MemberQuotesResponse.from(koAuthorUrl, enAuthorUrl, memberQuote)
        }
    }

    private fun getMemberQuotesWithContentByRequest(
        member: Member,
        request: MemberQuotesCommonRequest
    ): List<MemberQuote> {
        val memberQuotes =
            memberQuoteRepository.findAllByMemberAndCreatedAtBetween(member, request.startDate, request.endDate)

        return if(request.likeYn == "Y") {
            memberQuotes.filter { it.likeYn == "Y" }
        } else {
            memberQuotes.filter { it.isViewQuoteData() }
        }
    }

    @Transactional(readOnly = true)
    fun typingQuote(member: Member, dailyQuoteSeq: Long): MemberTypingQuoteResponse {
        val dailyQuote = dailyQuoteService.getDailyQuoteByDailQuoteSeq(dailyQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")
        val memberQuote = getMemberQuoteByDailyQuoteSeq(member, dailyQuote.dailyQuoteSeq)

        return MemberTypingQuoteResponse.from(dailyQuote, memberQuote)
    }

    @Transactional(readOnly = true)
    fun getMemberQuoteByDailyQuoteSeq(member: Member, dailyQuoteSeq: Long): MemberQuote? {
        return memberQuoteRepository.findByMemberAndDailyQuoteDailyQuoteSeq(member, dailyQuoteSeq)
    }

    @Transactional(readOnly = true)
    fun getMemberQuoteByMemberQuoteSeq(member: Member, memberQuoteSeq: Long): MemberQuote? {
        return memberQuoteRepository.findByMemberAndMemberQuoteSeq(member, memberQuoteSeq)
    }
}