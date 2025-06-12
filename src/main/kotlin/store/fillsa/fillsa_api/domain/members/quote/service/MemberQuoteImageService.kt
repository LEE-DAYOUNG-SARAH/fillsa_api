package store.fillsa.fillsa_api.domain.members.quote.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.common.service.FileService
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.quote.service.DailyQuoteService

@Service
class MemberQuoteImageService(
    private val fileService: FileService,
    private val memberQuoteReadService: MemberQuoteReadService,
    private val memberQuoteUpdateService: MemberQuoteUpdateService,
    private val dailyQuoteService: DailyQuoteService,
) {
    private val PATH = "members"

    fun uploadImage(member: Member, dailyQuoteSeq: Long, image: MultipartFile): Long {
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

        memberQuoteUpdateService.updateImagePath(memberQuote, fileUrl)

        return memberQuote.memberQuoteSeq
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