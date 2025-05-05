package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MemberTypingQuoteResponse(
    @Schema(description = "국문 명언")
    val korQuote: String?,

    @Schema(description = "영문 명언")
    val engQuote: String?,

    @Schema(description = "타이핑 국문 명언")
    val typingKorQuote: String?,

    @Schema(description = "타이핑 영문 명언")
    val typingEngQuote: String?,
)