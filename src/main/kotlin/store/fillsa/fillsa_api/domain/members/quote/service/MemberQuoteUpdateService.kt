package store.fillsa.fillsa_api.domain.members.quote.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberQuoteUpdateResult
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import store.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteResponse
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberQuoteRepository
import store.fillsa.fillsa_api.domain.quote.service.DailyQuoteService

@Service
class MemberQuoteUpdateService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteService: DailyQuoteService,
    private val memberQuoteReadService: MemberQuoteReadService,
    private val memberStreakService: MemberStreakService
) {
    @Transactional
    fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): TypingQuoteResponse {
        val dailyQuote = dailyQuoteService.getDailyQuoteByDailQuoteSeq(dailyQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")

        val memberQuote = memberQuoteReadService.getMemberQuoteByDailyQuoteSeq(member, dailyQuote.dailyQuoteSeq)
            ?: createMemberQuote(
                MemberQuote(
                    member = member,
                    dailyQuote = dailyQuote
                )
            )

        val wasCompleted = memberQuote.completed
        val wasTodayCompleted = memberQuote.todayCompleted

        if(memberQuote.shouldMarkTypingCompleted(
                request.typingKorQuote,
                request.typingEngQuote,
                dailyQuote.quote.korQuote,
                dailyQuote.quote.engQuote
        )) {
            memberQuote.complete(dailyQuote.quoteDate)
            memberStreakService.recordTodayCompletion(memberQuote.dailyQuote.quoteDate, memberQuote.member)
        }

        memberQuote.updateTypingQuote(request.typingKorQuote, request.typingEngQuote)

        val result = MemberQuoteUpdateResult.of(memberQuote, wasCompleted, wasTodayCompleted)

        return TypingQuoteResponse(
            memberQuoteSeq = result.memberQuote.memberQuoteSeq,
            completed = result.completedChanged,
            todayCompleted = result.todayCompletedChanged
        )
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
    fun updateImagePath(memberQuote: MemberQuote, imagePath: String?): MemberQuoteUpdateResult {
        val findMemberQuote = memberQuoteRepository.findByMemberQuoteSeq(memberQuote.memberQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 memberQuoteSeq: ${memberQuote.memberQuoteSeq}")

        val wasCompleted = findMemberQuote.completed
        val wasTodayCompleted = findMemberQuote.todayCompleted

        if(findMemberQuote.shouldMarkImageCompleted(imagePath)) {
            findMemberQuote.complete(findMemberQuote.dailyQuote.quoteDate)
            memberStreakService.recordTodayCompletion(findMemberQuote.dailyQuote.quoteDate, findMemberQuote.member)
        }

        findMemberQuote.updateImagePath(imagePath)

        return MemberQuoteUpdateResult.of(findMemberQuote, wasCompleted, wasTodayCompleted)
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