package com.fillsa.fillsa_api.domain.members.quote.service.useCase

import com.fillsa.fillsa_api.common.dto.PageResponse
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.MemberQuoteListRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.MemberQuoteListResponse
import com.fillsa.fillsa_api.domain.members.quote.dto.MemoRequest
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile

interface MemberQuoteUseCase {
    /**
     *  타이핑 명언 저장
     */
    fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long

    /**
     *  이미지 업로드
     */
    fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): Long

    /**
     *  이미지 삭제
     */
    fun deleteImage(member: Member, dailyQuoteSeq: Long): Long

    /**
     *  사용자 명언 목록 조회
     */
    fun getMemberQuotes(member: Member, pageable: Pageable, request: MemberQuoteListRequest):
        PageResponse<MemberQuoteListResponse>

    /**
     *  메모 저장
     */
    fun memo(member: Member, memberQuoteSeq: Long, request: MemoRequest): Long
}