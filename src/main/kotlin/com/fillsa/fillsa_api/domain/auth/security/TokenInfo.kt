package com.fillsa.fillsa_api.domain.auth.security

data class TokenInfo(
    val accessToken: String,    // 액세스 토큰
    val refreshToken: String,   // 리프레시 토큰
    val tokenType: String = "Bearer",  // 토큰 타입 (기본값: Bearer)
    val expiresIn: Long = 3600  // 액세스 토큰 만료 시간 (초 단위, 기본값: 1시간)
)