package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema

data class TypingQuoteRequest(
    @Schema(description = "타이핑 국문 명언")
    val typingKorQuote: String?,

    @Schema(description = "타이핑 영문 명언")
    val typingEngQuote: String?,
)

data class TypingQuoteResponseV2(
    @Schema(description = "사용자 명언 일련번호")
    val memberQuoteSeq: Long,

    @Schema(description = "필사 완료 변경여부")
    val completedChanged: Boolean,

    @Schema(description = "연속 필사 완료여부")
    val todayCompletedChanged: Boolean
)