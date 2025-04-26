package com.fillsa.fillsa_api.domain.auth.dto

data class TokenRefreshRequest(
    val refreshToken: String
)