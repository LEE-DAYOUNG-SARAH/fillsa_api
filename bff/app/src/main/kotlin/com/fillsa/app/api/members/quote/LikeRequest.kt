package com.fillsa.app.api.members.quote

import io.swagger.v3.oas.annotations.media.Schema

data class LikeRequest(
    @Schema(description = "좋아요 여부", example = "Y/N", required = true)
    val likeYn: String
)