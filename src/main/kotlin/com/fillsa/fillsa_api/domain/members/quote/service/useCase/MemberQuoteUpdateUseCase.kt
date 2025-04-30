package com.fillsa.fillsa_api.domain.members.quote.service.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.LikeRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote

interface MemberQuoteUpdateUseCase {
    /**
     *  타이핑 명언 저장
     */
    fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long

    /**
     *  메모 저장
     */
    fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long

    /**
     *  사용자 명언 생성
     */
    fun createMemberQuote(memberQuote: MemberQuote): MemberQuote

    /**
     *  이미지 경로 변경
     */
    fun updateImagePath(memberQuote: MemberQuote, imagePath: String?)

    /**
     *  좋아요 저장
     */
    fun like(member: Member, dailyQuoteSeq: Long, request: LikeRequest): Long
}