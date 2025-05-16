package store.fillsa.fillsa_api.domain.members.quote.dto

import store.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote

class MemberDailyQuoteResponse(
    @Schema(description = "좋아요 여부(Y/N)", required = true)
    val likeYn: String,

    @Schema(description = "s3 이미지 경로")
    val imagePath: String?,

    dailyQuoteSeq: Long,
    korQuote: String?,
    engQuote: String?,
    korAuthor: String?,
    engAuthor: String?,
    authorUrl: String?
): DailyQuoteResponse(dailyQuoteSeq, korQuote, engQuote, korAuthor, engAuthor, authorUrl) {
    companion object {
        fun from(
            koAuthorUrl: String,
            enAuthorUrl: String,
            dailyQuote: DailyQuote,
            memberQuote: MemberQuote?
        ) = MemberDailyQuoteResponse(
            dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
            korQuote = dailyQuote.quote.korQuote,
            engQuote = dailyQuote.quote.engQuote,
            korAuthor = dailyQuote.quote.korAuthor,
            engAuthor = dailyQuote.quote.engAuthor,
            authorUrl = dailyQuote.quote.korAuthor?.let { "${koAuthorUrl}$it" }
                ?: "${enAuthorUrl}${dailyQuote.quote.engAuthor}",
            likeYn = memberQuote?.likeYn ?: "N",
            imagePath = memberQuote?.imagePath
        )
    }
}
