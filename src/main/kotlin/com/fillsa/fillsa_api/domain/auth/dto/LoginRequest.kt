package com.fillsa.fillsa_api.domain.auth.dto

data class LoginRequest(
    val code: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val memberSeq: Long,
    val email: String,
    val nickname: String
)