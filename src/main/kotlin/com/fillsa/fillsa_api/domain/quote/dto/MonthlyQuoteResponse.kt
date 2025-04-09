package com.fillsa.fillsa_api.domain.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class MonthlyQuoteResponse(
    @Schema(description = "사용자 명언 정보")
    val memerQuotes: List<MemberQuoteData> = listOf(),

    @Schema(description = "월별 요약 정보")
    val monthlySummary: MonthlySummaryData
) {
    data class MemberQuoteData(
        @Schema(description = "일별 명언 일련번호")
        val dailyQuoteSeq: Long,

        @Schema(description = "명언 일자")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        val quoteDate: LocalDate,

        @Schema(description = "국문 명언")
        val korAuthor: String,

        @Schema(description = "타이핑 여부", example = "Y/N")
        val typingYn: String,

        @Schema(description = "좋아요 여부", example = "Y/N")
        val likeYn: String
    )

    data class MonthlySummaryData(
        val typingCount: Int,
        val likeCount: Int
    )
}