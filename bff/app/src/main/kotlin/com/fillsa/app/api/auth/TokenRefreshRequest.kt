package com.fillsa.app.api.auth

import io.swagger.v3.oas.annotations.media.Schema

data class TokenRefreshRequest(
    @Schema(description = "refresh token", required = true)
    val refreshToken: String,

    @Schema(description = "디바이스 id", required = true)
    val deviceId: String
)