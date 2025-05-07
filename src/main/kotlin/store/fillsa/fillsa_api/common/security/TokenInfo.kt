package store.fillsa.fillsa_api.common.security

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)