package com.fillsa.app.api.members.quote

import com.fillsa.app.api.quote.DailyQuoteResponse
import io.swagger.v3.oas.annotations.media.Schema
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.quote.DailyQuote

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
