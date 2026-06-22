package com.fillsa.app.common.security

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)