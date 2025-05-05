package store.fillsa.fillsa_api.domain.members.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class MemberMonthlyQuoteResponse(
    @Schema(description = "사용자 명언 정보")
    val memerQuotes: List<MemberQuotesData>,

    @Schema(description = "월별 요약 정보")
    val monthlySummary: MonthlySummaryData
) {
    data class MemberQuotesData(
        @Schema(description = "일별 명언 일련번호")
        val dailyQuoteSeq: Long,

        @Schema(description = "명언 일자")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        val quoteDate: LocalDate,

        @Schema(description = "명언")
        val quote: String,

        @Schema(description = "명언 저자")
        val author: String,

        @Schema(description = "타이핑 여부", example = "Y/N")
        val typingYn: String,

        @Schema(description = "좋아요 여부", example = "Y/N")
        val likeYn: String
    )

    data class MonthlySummaryData(
        @Schema(description = "타이핑 갯수")
        val typingCount: Int,

        @Schema(description = "좋아요 갯수")
        val likeCount: Int
    )
}