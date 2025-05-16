package store.fillsa.fillsa_api.domain.members.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import java.time.LocalDate

data class MemberQuotesRequest(
    @Schema(description = "좋아요 여부", required = true)
    val likeYn: String
)

data class MemberQuotesResponse(
    @Schema(description = "사용자 명언 일련번호", required = true)
    val memberQuoteSeq: Long,

    @Schema(description = "명언 일자", example = "yyyy-MM-dd", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    val quoteDate: LocalDate,

    @Schema(description = "명언 요일", example = "월/화/수", required = true)
    val quoteDayOfWeek: String,

    @Schema(description = "명언", required = true)
    val quote: String,

    @Schema(description = "저자", required = true)
    val author: String,

    @Schema(description = "저자 url")
    val authorUrl: String?,

    @Schema(description = "메모")
    val memo: String?,

    @Schema(description = "메모 여부", example = "Y/N", required = true)
    val memoYn: String,

    @Schema(description = "좋아요 여부", example = "Y/N", required = true)
    val likeYn: String
) {
    companion object {
        fun from(koAuthorUrl: String, enAuthorUrl: String, memberQuote: MemberQuote) = MemberQuotesResponse(
            memberQuoteSeq = memberQuote.memberQuoteSeq,
            quoteDate = memberQuote.dailyQuote.quoteDate,
            quoteDayOfWeek = memberQuote.dailyQuote.quoteDate.dayOfWeek.toString(),
            quote = memberQuote.dailyQuote.quote.korQuote ?: memberQuote.dailyQuote.quote.engQuote.orEmpty(),
            author = memberQuote.dailyQuote.quote.korAuthor ?: memberQuote.dailyQuote.quote.engAuthor.orEmpty(),
            authorUrl = memberQuote.dailyQuote.quote.korAuthor?.let { "${koAuthorUrl}$it" }
                ?: "${enAuthorUrl}${memberQuote.dailyQuote.quote.engAuthor}",
            memo = memberQuote.memo,
            memoYn = if (memberQuote.memo.isNullOrBlank()) "N" else "Y",
            likeYn = memberQuote.likeYn
        )
    }
}