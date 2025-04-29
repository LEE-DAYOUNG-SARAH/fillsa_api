package com.fillsa.fillsa_api.domain.members.quote.service.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.web.multipart.MultipartFile

interface MemberQuoteImageUseCase {
    /**
     *  이미지 업로드
     */
    fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): Long

    /**
     *  이미지 삭제
     */
    fun deleteImage(member: Member, dailyQuoteSeq: Long): Long
}