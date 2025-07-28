package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class MemberQuotesCommonRequest(
    @Schema(description = "좋아요 여부", required = true)
    val likeYn: String,

    @Schema(description = "시작일자", required = true)
    val startDate: LocalDate,

    @Schema(description = "종료일자", required = true)
    val endDate: LocalDate
) {
    companion object {
        fun fromV1(v1: MemberQuotesRequest) = MemberQuotesCommonRequest(
            likeYn = v1.likeYn,
            startDate = LocalDate.now().minusYears(1),
            endDate = LocalDate.now()
        )

        fun fromV2(v2: MemberQuotesRequestV2) = MemberQuotesCommonRequest(
            likeYn = v2.likeYn,
            startDate = v2.startDate,
            endDate = v2.endDate
        )
    }
}