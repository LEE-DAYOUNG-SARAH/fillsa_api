package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema

data class TypingQuoteRequest(
    @Schema(description = "타이핑 국문 명언")
    val typingKorQuote: String?,

    @Schema(description = "타이핑 영문 명언")
    val typingEngQuote: String?,
)