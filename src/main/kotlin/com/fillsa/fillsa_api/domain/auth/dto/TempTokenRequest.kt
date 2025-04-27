package com.fillsa.fillsa_api.domain.auth.dto

data class TempTokenRequest(
    val tempToken: String,
    val deviceId: String
)