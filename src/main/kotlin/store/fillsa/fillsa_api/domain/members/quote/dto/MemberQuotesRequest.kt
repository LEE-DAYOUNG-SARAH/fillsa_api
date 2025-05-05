package store.fillsa.fillsa_api.domain.members.quote.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class MemberQuotesRequest(
    @Schema(description = "좋아요 여부")
    val likeYn: String
)

data class MemberQuotesResponse(
    @Schema(description = "사용자 명언 일련번호")
    val memberQuoteSeq: Long,

    @Schema(description = "명언 일자", example = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    val quoteDate: LocalDate,

    @Schema(description = "명언 요일", example = "월/화/수")
    val quoteDayOfWeek: String,

    @Schema(description = "명언")
    val quote: String,

    @Schema(description = "저자")
    val author: String,

    @Schema(description = "저자 url")
    val authorUrl: String?,

    @Schema(description = "메모")
    val memo: String?,

    @Schema(description = "메모 여부", example = "Y/N")
    val memoYn: String,

    @Schema(description = "좋아요 여부", example = "Y/N")
    val likeYn: String
)