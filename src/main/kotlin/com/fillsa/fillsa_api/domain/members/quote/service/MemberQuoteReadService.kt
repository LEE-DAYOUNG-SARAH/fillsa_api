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

    override fun monthlyQuotes(yearMonth: YearMonth): MemberMonthlyQuoteResponse {
        TODO("Not yet implemented")
    }

    override fun memberQuotes(member: Member, pageable: Pageable, request: MemberQuotesRequest): PageResponse<MemberQuotesResponse> {
        TODO("Not yet implemented")
    }

    override fun typingQuote(member: Member, dailyQuoteSeq: Long): MemberTypingQuoteResponse {
        TODO("Not yet implemented")
    }

    override fun getMemberQuoteByDailyQuoteSeq(member: Member, dailyQuoteSeq: Long): MemberQuote? {
        return memberQuoteRepository.findByMemberAndDailyQuoteDailyQuoteSeq(member, dailyQuoteSeq)
    }

    override fun getMemberQuoteByMemberQuoteSeq(member: Member, memberQuoteSeq: Long): MemberQuote? {
        return memberQuoteRepository.findByMemberAndMemberQuoteSeq(member, memberQuoteSeq)
    }
}