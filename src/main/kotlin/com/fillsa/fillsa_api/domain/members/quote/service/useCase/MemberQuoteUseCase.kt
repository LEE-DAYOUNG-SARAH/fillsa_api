package com.fillsa.fillsa_api.domain.members.quote.service.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest

interface MemberQuoteUseCase {
    /**
     *  타이핑 명언 저장
     */
    fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long
}