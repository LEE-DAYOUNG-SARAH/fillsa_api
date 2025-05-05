package store.fillsa.fillsa_api.domain.members.quote.dto

import store.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import io.swagger.v3.oas.annotations.media.Schema

class MemberDailyQuoteResponse(
    @Schema(description = "좋아요 여부")
    val likeYn: String,

    @Schema(description = "s3 이미지 경로")
    val imagePath: String?,

    dailyQuoteSeq: Long,
    korQuote: String?,
    engQuote: String?,
    korAuthor: String?,
    engAuthor: String?,
    authorUrl: String?
): DailyQuoteResponse(dailyQuoteSeq, korQuote, engQuote, korAuthor, engAuthor, authorUrl)
