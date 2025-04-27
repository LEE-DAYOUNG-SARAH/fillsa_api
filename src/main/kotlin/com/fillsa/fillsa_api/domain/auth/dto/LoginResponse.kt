package com.fillsa.fillsa_api.domain.auth.dto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val memberSeq: Long,
    val nickname: String,
    val profileImageUrl: String?
)