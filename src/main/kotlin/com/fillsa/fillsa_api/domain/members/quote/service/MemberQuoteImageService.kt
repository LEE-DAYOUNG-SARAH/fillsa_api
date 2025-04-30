package com.fillsa.fillsa_api.domain.members.quote.service

import com.fillsa.fillsa_api.common.exception.NotFoundException
import com.fillsa.fillsa_api.common.service.FileService
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteImageUseCase
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteReadUseCase
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteUpdateUseCase
import com.fillsa.fillsa_api.domain.quote.service.useCase.DailyQuoteUseCase
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MemberQuoteImageService(
    private val fileService: FileService,
    private val memberQuoteReadUseCase: MemberQuoteReadUseCase,
    private val memberQuoteUpdateUseCase: MemberQuoteUpdateUseCase,
    private val dailyQuoteUseCase: DailyQuoteUseCase,
): MemberQuoteImageUseCase {
    private val PATH = "member-quotes/"

    override fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): Long {
        val dailyQuote = dailyQuoteUseCase.getDailyQuote(dailyQuoteSeq)
            ?: throw NotFoundException("존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")

        val memberQuote = memberQuoteReadUseCase.getMemberQuote(member, dailyQuote.dailyQuoteSeq)
            ?: memberQuoteUpdateUseCase.createMemberQuote(
                MemberQuote(
                    member = member,
                    dailyQuote = dailyQuote,
                    likeYn = "N"
                )
            )

        val filePath = "$PATH/${memberQuote.memberQuoteSeq}"
        val fileUrl = memberQuote.imagePath?.let {
            fileService.updateFile(filePath, image, it)
        } ?: fileService.uploadFile(filePath, image)

        memberQuoteUpdateUseCase.updateImagePath(memberQuote, fileUrl)

        return memberQuote.memberQuoteSeq
    }

    override fun deleteImage(member: Member, dailyQuoteSeq: Long): Long {
        TODO("Not yet implemented")
    }
}