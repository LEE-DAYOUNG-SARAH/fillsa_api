package store.fillsa.fillsa_api.domain.members.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
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
)