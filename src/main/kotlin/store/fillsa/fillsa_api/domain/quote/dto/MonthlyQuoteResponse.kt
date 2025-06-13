package store.fillsa.fillsa_api.domain.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import java.time.LocalDate

data class MonthlyQuoteResponse(
    @Schema(description = "일별 명언 일련번호", required = true)
    val dailyQuoteSeq: Long,

    @Schema(description = "명언 일자", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    val quoteDate: LocalDate,

    @Schema(description = "명언", required = true)
    val quote: String,

    @Schema(description = "명언 저자", required = true)
    val author: String
) {
    companion object {
        fun from(dailyQuote: DailyQuote) = MonthlyQuoteResponse(
            dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
            quoteDate = dailyQuote.quoteDate,
            quote = dailyQuote.quote.korQuote ?: dailyQuote.quote.engQuote.orEmpty(),
            author = dailyQuote.quote.korAuthor ?: dailyQuote.quote.engAuthor.orEmpty()
        )
    }
}