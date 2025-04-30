package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.common.exception.NotFoundException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.*
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import com.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteReadUseCase
import com.fillsa.fillsa_api.domain.quote.service.useCase.DailyQuoteUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth

@Service
class MemberQuoteReadService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteUseCase: DailyQuoteUseCase,
    @Value("\${fillsa.ko-author-url}")
    private val koAuthorUrl: String,
    @Value("\${fillsa.en-author-url}")
    private val enAuthorUrl: String,
): MemberQuoteReadUseCase {

    @Transactional(readOnly = true)
    override fun dailyQuote(member: Member, quoteDate: LocalDate): MemberDailyQuoteResponse {
        val dailyQuote = dailyQuoteUseCase.getDailyQuoteByQuoteDate(quoteDate)
            ?: throw NotFoundException("존재하지 않는 quoteDate: $quoteDate")

        val memberQuote = memberQuoteRepository.findByMemberAndDailyQuote(member, dailyQuote)

        return MemberDailyQuoteResponse(
            dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
            korQuote = dailyQuote.quote.korQuote,
            engQuote = dailyQuote.quote.engQuote,
            korAuthor = dailyQuote.quote.korAuthor,
            engAuthor = dailyQuote.quote.engAuthor,
            authorUrl = dailyQuote.quote.korAuthor?.let { "${koAuthorUrl}$it" }
                ?: "${enAuthorUrl}${dailyQuote.quote.engAuthor}",
            likeYn = memberQuote?.likeYn ?: "N",
            imagePath = memberQuote?.imagePath
        )
    }

    @Transactional(readOnly = true)
    override fun monthlyQuotes(member: Member, yearMonth: YearMonth): MemberMonthlyQuoteResponse {
        val memberQuotes = memberQuoteRepository.findByMemberAndQuoteDateBetween(
            member = member,
            beginQuoteDate = yearMonth.atDay(1),
            endQuoteDate = yearMonth.atEndOfMonth()
        )

        val memberQuoteData = memberQuotes.map {
            MemberMonthlyQuoteResponse.MemberQuotesData(
                dailyQuoteSeq = it.dailyQuote.dailyQuoteSeq,
                quoteDate = it.dailyQuote.quoteDate,
                quote = it.dailyQuote.quote.korQuote ?: it.dailyQuote.quote.engQuote.orEmpty(),
                author = it.dailyQuote.quote.korAuthor ?: it.dailyQuote.quote.engAuthor.orEmpty(),
                typingYn = it.getTypingYn(),
                likeYn = it.likeYn
            )
        }

        val monthlySummary = MemberMonthlyQuoteResponse.MonthlySummaryData(
            typingCount = memberQuoteData.count { it.typingYn == "Y" },
            likeCount = memberQuoteData.count { it.likeYn == "Y" }
        )

        return MemberMonthlyQuoteResponse(memberQuoteData, monthlySummary)
    }

    override fun memberQuotes(member: Member, pageable: Pageable, request: MemberQuotesRequest): PageResponse<MemberQuotesResponse> {
        TODO("Not yet implemented")
    }

    override fun typingQuote(member: Member, dailyQuoteSeq: Long): MemberTypingQuoteResponse {
        TODO("Not yet implemented")
    }

    @Transactional(readOnly = true)
    override fun getMemberQuoteByDailyQuoteSeq(member: Member, dailyQuoteSeq: Long): MemberQuote? {
        return memberQuoteRepository.findByMemberAndDailyQuoteDailyQuoteSeq(member, dailyQuoteSeq)
    }

    @Transactional(readOnly = true)
    override fun getMemberQuoteByMemberQuoteSeq(member: Member, memberQuoteSeq: Long): MemberQuote? {
        return memberQuoteRepository.findByMemberAndMemberQuoteSeq(member, memberQuoteSeq)
    }
}