package com.fillsa.fillsa_api.domain.members.quote.service.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest

interface MemberQuoteUpdateUseCase {
    /**
     *  타이핑 명언 저장
     */
    fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long

    /**
     *  메모 저장
     */
    fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long
}