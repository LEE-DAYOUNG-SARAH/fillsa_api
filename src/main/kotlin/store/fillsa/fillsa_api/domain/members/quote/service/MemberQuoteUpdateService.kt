package store.fillsa.fillsa_api.domain.members.quote.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import store.fillsa.fillsa_api.domain.quote.service.DailyQuoteService

@Service
class MemberQuoteUpdateService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteService: DailyQuoteService,
    private val memberQuoteReadService: MemberQuoteReadService
) {
    @Transactional
    fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long {
        val dailyQuote = dailyQuoteService.getDailyQuoteByDailQuoteSeq(dailyQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")

        val memberQuote = memberQuoteReadService.getMemberQuoteByDailyQuoteSeq(member, dailyQuote.dailyQuoteSeq)
            ?: createMemberQuote(
                MemberQuote(
                    member = member,
                    dailyQuote = dailyQuote
                )
            )

        memberQuote.updateTypingQuote(request.typingKorQuote, request.typingEngQuote)

        return memberQuote.memberQuoteSeq
    }

    @Transactional
    fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long {
        val memberQuote = memberQuoteReadService.getMemberQuoteByMemberQuoteSeq(member, memberQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 memberQuoteSeq: $memberQuoteSeq")

        memberQuote.updateMemo(request.memo)

        return memberQuote.memberQuoteSeq
    }

    @Transactional
    fun createMemberQuote(memberQuote: MemberQuote): MemberQuote {
        return memberQuoteRepository.save(memberQuote)
    }

    @Transactional
    fun updateImagePath(memberQuote: MemberQuote, imagePath: String?) {
        val findMemberQuote = memberQuoteRepository.findById(memberQuote.memberQuoteSeq)
            .orElseThrow { BusinessException(NOT_FOUND, "존재하지 않는 memberQuoteSeq: ${memberQuote.memberQuoteSeq}") }

        findMemberQuote.updateImagePath(imagePath)
    }

    @Transactional
    fun like(member: Member, dailyQuoteSeq: Long, request: LikeRequest): Long {
        val dailyQuote = dailyQuoteService.getDailyQuoteByDailQuoteSeq(dailyQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")

        val memberQuote = memberQuoteReadService.getMemberQuoteByDailyQuoteSeq(member, dailyQuote.dailyQuoteSeq)
            ?: createMemberQuote(
                MemberQuote(
                    member = member,
                    dailyQuote = dailyQuote
                )
            )

        memberQuote.updateLikeYn(request.likeYn)

        return memberQuote.memberQuoteSeq
    }
}