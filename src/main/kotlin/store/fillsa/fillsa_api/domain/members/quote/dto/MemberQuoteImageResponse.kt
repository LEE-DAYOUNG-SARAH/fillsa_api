package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema

data class MemberQuoteImageResponse(
    @Schema(description = "사용자 명언 일련번호")
    val memberQuoteSeq: Long,

    @Schema(description = "s3 주소")
    val imagePath: String,

    @Schema(description = "필사 완료 변경여부")
    val completedChanged: Boolean,

    @Schema(description = "연속 필사 변경여부")
    val todayCompletedChanged: Boolean
) {
    companion object {
        fun from(result: MemberQuoteUpdateResult) = MemberQuoteImageResponse(
            memberQuoteSeq = result.memberQuote.memberQuoteSeq,
            imagePath = result.memberQuote.imagePath.orEmpty(),
            completedChanged = result.completedChanged,
            todayCompletedChanged = result.todayCompletedChanged
        )
    }
}