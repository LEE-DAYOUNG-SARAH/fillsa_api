package com.fillsa.app.api.quote

import io.swagger.v3.oas.annotations.media.Schema
import com.fillsa.service.quote.DailyQuote

open class DailyQuoteResponse(
    @Schema(description = "일별 명언 일련번호", required = true)
    val dailyQuoteSeq: Long,

    @Schema(description = "국문 명언")
    val korQuote: String?,

    @Schema(description = "영문 명언")
    val engQuote: String?,

    @Schema(description = "국문 저자")
    val korAuthor: String?,

    @Schema(description = "영문 저자")
    val engAuthor: String?,

    @Schema(description = "저자 url")
    val authorUrl: String?
) {
    companion object {
        fun from(
            koAuthorUrl: String,
            enAuthorUrl: String,
            dailyQuote: DailyQuote,
        ) = DailyQuoteResponse(
            dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
            korQuote = dailyQuote.quote.korQuote,
            engQuote = dailyQuote.quote.engQuote,
            korAuthor = dailyQuote.quote.korAuthor,
            engAuthor = dailyQuote.quote.engAuthor,
            authorUrl = dailyQuote.quote.korAuthor?.let { "${koAuthorUrl}$it" }
                ?: "${enAuthorUrl}${dailyQuote.quote.engAuthor}"
        )
    }
}