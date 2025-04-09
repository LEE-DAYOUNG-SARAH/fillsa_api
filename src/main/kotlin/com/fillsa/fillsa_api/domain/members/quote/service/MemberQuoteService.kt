package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.dto.TypingQuoteRequest
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUseCase
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MemberQuoteService(): MemberQuoteUseCase {

    override fun typingQuote(member: Member, dailyQuoteSeq: Long, request: TypingQuoteRequest): Long {
        // TODO. 사용자 명언 조회
        // TODO. 사용자 명언 생성 or 수정
        // TODO. 사용자 명언 seq 반환
        return 1L
    }

    override fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): Long {
        // TODO. 사용자 명언 조회
        // TODO. 사용자 명언 생성 or 수정
        // TODO. S3 이미지 업로드 및 삭제
        // TODO. 사용자 명언 수정
        // TODO. 사용자 명언 seq 반환
        return 1L
    }
}