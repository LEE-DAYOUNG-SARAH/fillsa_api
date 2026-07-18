package com.fillsa.app.service.members.quote

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode.NOT_FOUND
import com.fillsa.app.common.service.FileService
import com.fillsa.service.member.Member
import com.fillsa.app.api.members.quote.MemberQuoteImageResponse
import com.fillsa.service.member.MemberQuote
import com.fillsa.app.service.quote.DailyQuoteService

@Service
class MemberQuoteImageService(
    private val fileService: FileService,
    private val memberQuoteReadService: MemberQuoteReadService,
    private val memberQuoteUpdateService: MemberQuoteUpdateService,
    private val dailyQuoteService: DailyQuoteService,
) {
    private val PATH = "members"

    fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): MemberQuoteImageResponse {
        val dailyQuote = dailyQuoteService.getDailyQuoteByDailQuoteSeq(dailyQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 dailyQuoteSeq: $dailyQuoteSeq")

        val memberQuote = memberQuoteReadService.getMemberQuoteByDailyQuoteSeq(member, dailyQuote.dailyQuoteSeq)
            ?: memberQuoteUpdateService.createMemberQuote(
                MemberQuote(
                    member = member,
                    dailyQuote = dailyQuote
                )
            )

        val filePath = "$PATH/${member.memberSeq}"
        val fileUrl = memberQuote.imagePath?.let {
            fileService.updateFile(filePath, image, it)
        } ?: fileService.uploadFile(filePath, image)

        val result = memberQuoteUpdateService.updateImagePath(memberQuote, fileUrl)

        return MemberQuoteImageResponse.from(result)
    }

    fun deleteImage(member: Member, dailyQuoteSeq: Long): Long {
        val memberQuote = memberQuoteReadService.getMemberQuoteByDailyQuoteSeq(member, dailyQuoteSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 memberQuote")

        memberQuote.imagePath?.let {
            fileService.deleteFile(it)
            memberQuoteUpdateService.updateImagePath(memberQuote, null)
        }

        return memberQuote.memberQuoteSeq
    }
}