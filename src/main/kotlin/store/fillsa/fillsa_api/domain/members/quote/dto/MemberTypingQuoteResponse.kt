package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote

data class MemberTypingQuoteResponse(
    @Schema(description = "국문 명언")
    val korQuote: String?,

    @Schema(description = "영문 명언")
    val engQuote: String?,

    @Schema(description = "타이핑 국문 명언")
    val typingKorQuote: String?,

    @Schema(description = "타이핑 영문 명언")
    val typingEngQuote: String?,
) {
    companion object {
        fun from(dailyQuote: DailyQuote, memberQuote: MemberQuote?) = MemberTypingQuoteResponse(
            korQuote = dailyQuote.quote.korQuote,
            engQuote = dailyQuote.quote.engQuote,
            typingKorQuote = memberQuote?.typingKorQuote,
            typingEngQuote = memberQuote?.typingEngQuote
        )
    }
}