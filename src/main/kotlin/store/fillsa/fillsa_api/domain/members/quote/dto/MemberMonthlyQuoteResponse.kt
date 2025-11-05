package store.fillsa.fillsa_api.domain.members.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
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
        fun from(quotes: List<DailyQuote>, memberQuotes: List<MemberQuote>): MemberMonthlyQuoteResponse {
            val memberQuoteData = quotes.map { dailyQuote ->
                val memberQuote = memberQuotes.find { it.dailyQuote.dailyQuoteSeq == dailyQuote.dailyQuoteSeq }
                MemberQuotesData(
                    dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
                    quoteDate = dailyQuote.quoteDate,
                    quote = dailyQuote.quote.korQuote ?: dailyQuote.quote.engQuote.orEmpty(),
                    author = dailyQuote.quote.korAuthor ?: dailyQuote.quote.engAuthor.orEmpty(),
                    typingYn = memberQuote?.getTypingYn() ?: "N",
                    likeYn = memberQuote?.likeYn ?: "N"
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