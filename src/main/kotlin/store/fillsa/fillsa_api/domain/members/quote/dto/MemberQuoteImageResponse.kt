package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote

data class MemberQuoteImageResponse(
    @Schema(description = "사용자 명언 일련번호")
    val memberQuoteSeq: Long,

    @Schema(description = "s3 주소")
    val imagePath: String
) {
    companion object {
        fun from(memberQuote: MemberQuote) = MemberQuoteImageResponse(
            memberQuoteSeq = memberQuote.memberQuoteSeq,
            imagePath = memberQuote.imagePath.orEmpty()
        )
    }
}