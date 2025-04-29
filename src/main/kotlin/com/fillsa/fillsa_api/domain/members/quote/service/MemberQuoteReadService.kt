package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.*
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteReadUseCase
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class MemberQuoteReadService: MemberQuoteReadUseCase {
    override fun dailyQuote(member: Member, quoteDate: LocalDate): MemberDailyQuoteResponse {
        TODO("Not yet implemented")
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
}