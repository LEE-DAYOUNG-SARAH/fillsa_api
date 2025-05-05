package store.fillsa.fillsa_api.domain.members.quote.dto

import io.swagger.v3.oas.annotations.media.Schema

data class LikeRequest(
    @Schema(description = "좋아요 여부", example = "Y/N")
    val likeYn: String
)