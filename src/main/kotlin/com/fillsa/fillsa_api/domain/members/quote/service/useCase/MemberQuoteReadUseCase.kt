package com.fillsa.fillsa_api.domain.members.quote.service.useCase

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.*
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.YearMonth

interface MemberQuoteReadUseCase {
    /*
    *  일별 명언 조회
    */
    fun dailyQuote(member: Member, quoteDate: LocalDate): MemberDailyQuoteResponse

    /**
     *  월별 명언 조회
     */
    fun monthlyQuotes(yearMonth: YearMonth): MemberMonthlyQuoteResponse

    /**
     *  사용자 명언 목록 조회
     */
    fun memberQuotes(member: Member, pageable: Pageable, request: MemberQuotesRequest):
        PageResponse<MemberQuotesResponse>

    /**
     *  타이핑 명언 조회
     */
    fun typingQuote(member: Member, dailyQuoteSeq: Long): MemberTypingQuoteResponse
}