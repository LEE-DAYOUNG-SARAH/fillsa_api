package store.fillsa.fillsa_api.domain.members.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import java.time.LocalDate

data class MemberMonthlyQuoteResponse(
    @Schema(description = "사용자 명언 정보", required = true)
    val memberQuotes: List<MemberQuotesData>,

    @Schema(description = "월별 요약 정보", required = true)
    val monthlySummary: MonthlySummaryData
) {
    data class MemberQuotesData(
        @Schema(description = "일별 명언 일련번호", required = true)
        val dailyQuoteSeq: Long,

        @Schema(description = "명언 일자", required = true)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        val quoteDate: LocalDate,

        @Schema(description = "명언", required = true)
        val quote: String,

        @Schema(description = "명언 저자", required = true)
        val author: String,

        @Schema(description = "타이핑 여부", example = "Y/N")
        val typingYn: String,

        @Schema(description = "좋아요 여부", example = "Y/N", required = true)
        val likeYn: String
    )

    data class MonthlySummaryData(
        @Schema(description = "타이핑 갯수", required = true)
        val typingCount: Int,

        @Schema(description = "좋아요 갯수", required = true)
        val likeCount: Int
    )

    companion object {
        fun from(memberQuotes: List<MemberQuote>): MemberMonthlyQuoteResponse {
            val memberQuoteData = memberQuotes.map {
                MemberQuotesData(
                    dailyQuoteSeq = it.dailyQuote.dailyQuoteSeq,
                    quoteDate = it.dailyQuote.quoteDate,
                    quote = it.dailyQuote.quote.korQuote ?: it.dailyQuote.quote.engQuote.orEmpty(),
                    author = it.dailyQuote.quote.korAuthor ?: it.dailyQuote.quote.engAuthor.orEmpty(),
                    typingYn = it.getTypingYn(),
                    likeYn = it.likeYn
                )
            }

            return MemberMonthlyQuoteResponse(
                memberQuotes = memberQuoteData,
                monthlySummary = MonthlySummaryData(
                    typingCount = memberQuoteData.count { it.typingYn == "Y" },
                    likeCount = memberQuoteData.count { it.likeYn == "Y" }
                )
            )
        }
    }
}