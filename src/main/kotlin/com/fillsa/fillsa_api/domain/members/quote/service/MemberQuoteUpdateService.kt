package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.common.exception.NotFoundException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import com.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUpdateUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
@Service
class MemberQuoteUpdateService(
    private val memberQuoteRepository: MemberQuoteRepository
): MemberQuoteUpdateUseCase {
    @Transactional
    override fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun createMemberQuote(memberQuote: MemberQuote): MemberQuote {
        return memberQuoteRepository.save(memberQuote)
    }

    @Transactional
    override fun updateImagePath(memberQuote: MemberQuote, imagePath: String?) {
        val findMemberQuote = memberQuoteRepository.findById(memberQuote.memberQuoteSeq)
            .orElseThrow { NotFoundException("존재하지 않는 memberQuoteSeq: ${memberQuote.memberQuoteSeq}") }

        findMemberQuote.updateImagePath(imagePath)
    }
}