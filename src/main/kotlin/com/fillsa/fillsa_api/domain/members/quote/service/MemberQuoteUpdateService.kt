package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUpdateUseCase
import org.springframework.stereotype.Service

@Service
class MemberQuoteUpdateService: MemberQuoteUpdateUseCase {
    override fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long {
        TODO("Not yet implemented")
    }

    override fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long {
        TODO("Not yet implemented")
    }
}