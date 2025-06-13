package store.fillsa.fillsa_api.domain.members.quote.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.dto.PageResponse
import store.fillsa.fillsa_api.common.exception.BusinessException
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
        val quotes = dailyQuoteService.getDailyQuoteByQuotMonth(yearMonth)
        val memberQuotes = getMemberQuotesWithContentByMonth(member, yearMonth)

        return MemberMonthlyQuoteResponse.from(quotes, memberQuotes)
    }

    private fun getMemberQuotesWithContentByMonth(member: Member, yearMonth: YearMonth): List<MemberQuote> {
        val memberQuotes = memberQuoteRepository.findAllByMemberAndQuoteDateBetween(
            member = member,
            beginQuoteDate = yearMonth.atDay(1),
            endQuoteDate = yearMonth.atEndOfMonth()
        )

        return memberQuotes.filter { it.hasContent() }
    }

    @Transactional(readOnly = true)
    fun memberQuotes(
        member: Member,
        pageable: Pageable,
        request: MemberQuotesRequest
    ): PageResponse<MemberQuotesResponse> {
        val memberQuotes = getMemberQuotesWithContentByLikeYn(member, request.likeYn)

        return PageResponse.fromList(memberQuotes, pageable) { memberQuote ->
            MemberQuotesResponse.from(koAuthorUrl, enAuthorUrl, memberQuote)
        }
    }

    private fun getMemberQuotesWithContentByLikeYn(member: Member, likeYn: String): List<MemberQuote> {
        val memberQuotes = memberQuoteRepository.findAllByMember(member)

        return if(likeYn == "Y") {
            memberQuotes.filter { it.likeYn == "Y" }
        } else {
            memberQuotes.filter { it.hasContent() }
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