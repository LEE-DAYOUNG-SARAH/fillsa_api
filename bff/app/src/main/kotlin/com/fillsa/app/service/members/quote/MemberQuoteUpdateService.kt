package com.fillsa.app.service.members.quote

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode.INVALID_REQUEST
import com.fillsa.util.exception.ErrorCode.NOT_FOUND
import com.fillsa.service.member.Member
import com.fillsa.app.api.members.quote.*
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.member.MemberQuoteRepository
import com.fillsa.app.service.quote.DailyQuoteService
import java.time.LocalDate

@Service
class MemberQuoteUpdateService(
    private val memberQuoteRepository: MemberQuoteRepository,
    private val dailyQuoteService: DailyQuoteService,
    private val memberQuoteReadService: MemberQuoteReadService,
    private val memberStreakService: MemberStreakService
) {
    @Transactional
    fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): TypingQuoteResponseV2 {
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

        return TypingQuoteResponseV2(
            memberQuoteSeq = result.memberQuote.memberQuoteSeq,
            completedChanged = result.completedChanged,
            todayCompletedChanged = result.todayCompletedChanged
        )
    }

    /**
     * 오늘의 질문 답변 저장 (등록·수정 겸용).
     *
     * 답변은 필사 완료(completed)·연속(streak)에 반영하지 않는다 — 별개 기록이다.
     * 근거: docs/home-renewal-api-plan.md §11-1
     */
    @Transactional
    fun saveAnswer(member: Member, dailyQuoteSeq: Long, request: AnswerRequest): AnswerResponse {
        val answer = request.answer.trim()
        if (answer.isEmpty()) {
            throw BusinessException(INVALID_REQUEST, "답변은 공백만 입력할 수 없습니다.")
        }
        if (answer.length > AnswerRequest.MAX_LENGTH) {
            throw BusinessException(INVALID_REQUEST, "답변은 최대 ${AnswerRequest.MAX_LENGTH}자입니다.")
        }

        val dailyQuote = dailyQuoteService.getDailyQuoteByDailQuoteSeq(dailyQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")

        if (dailyQuote.quoteDate.isAfter(LocalDate.now())) {
            throw BusinessException(INVALID_REQUEST, "미래 날짜에는 답변할 수 없습니다.")
        }

        val memberQuote = memberQuoteReadService.getMemberQuoteByDailyQuoteSeq(member, dailyQuote.dailyQuoteSeq)
            ?: createMemberQuote(
                MemberQuote(
                    member = member,
                    dailyQuote = dailyQuote
                )
            )

        memberQuote.updateAnswer(answer)

        return AnswerResponse.from(memberQuote)
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