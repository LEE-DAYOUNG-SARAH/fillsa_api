package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.common.exception.NotFoundException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import com.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteReadUseCase
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUpdateUseCase
import com.fillsa.fillsa_api.domain.quote.service.useCase.DailyQuoteUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
@Service
class MemberQuoteUpdateService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteUseCase: DailyQuoteUseCase,
    private val memberQuoteReadUseCase: MemberQuoteReadUseCase
): MemberQuoteUpdateUseCase {
    @Transactional
    override fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long {
        val dailyQuote = dailyQuoteUseCase.getDailyQuoteByDailQuoteSeq(dailyQuoteSeq)
            ?: throw NotFoundException("존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")

        val memberQuote = memberQuoteReadUseCase.getMemberQuoteByDailyQuoteSeq(member, dailyQuote.dailyQuoteSeq)
            ?: createMemberQuote(
                MemberQuote(
                    member = member,
                    dailyQuote = dailyQuote,
                    likeYn = "N"
                )
            )

        memberQuote.updateTypingQuote(request.typingKorQuote, request.typingEngQuote)

        return memberQuote.memberQuoteSeq
    }

    @Transactional
    override fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long {
        val memberQuote = memberQuoteReadUseCase.getMemberQuoteByMemberQuoteSeq(member, memberQuoteSeq)
            ?: throw NotFoundException("존재하지 않는 memberQuoteSeq: $memberQuoteSeq")

        memberQuote.updateMemo(request.memo)

        return memberQuote.memberQuoteSeq
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