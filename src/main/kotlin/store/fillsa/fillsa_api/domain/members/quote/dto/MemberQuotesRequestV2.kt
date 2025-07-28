package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class MemberQuotesRequestV2 (
    @Schema(description = "좋아요 여부", required = true)
    val likeYn: String,

    @Schema(description = "시작일자", required = true)
    val startDate: LocalDate,

    @Schema(description = "종료일자", required = true)
    val endDate: LocalDate
)