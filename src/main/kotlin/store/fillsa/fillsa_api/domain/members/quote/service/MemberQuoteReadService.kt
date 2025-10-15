package store.fillsa.fillsa_api.domain.members.quote.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.dto.PageResponse
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.*
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import store.fillsa.fillsa_api.domain.quote.service.DailyQuoteService
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