package store.fillsa.fillsa_api.domain.auth.dto

data class TokenRefreshRequest(
    val refreshToken: String,
    val deviceId: String
)