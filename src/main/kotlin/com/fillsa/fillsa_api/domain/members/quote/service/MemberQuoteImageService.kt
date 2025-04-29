package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteImageUseCase
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MemberQuoteImageService(): MemberQuoteImageUseCase {

    override fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): Long {
        TODO("Not yet implemented")
    }

    override fun deleteImage(member: Member, dailyQuoteSeq: Long): Long {
        TODO("Not yet implemented")
    }
}